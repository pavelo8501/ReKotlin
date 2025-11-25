package po.misc.data.pretty_print.rows


import po.misc.collections.asList
import po.misc.context.tracable.TraceableContext
import po.misc.counters.LogJournal
import po.misc.data.TextBuilder
import po.misc.data.output.output
import po.misc.data.pretty_print.parts.Console220
import po.misc.data.pretty_print.parts.RenderDefaults
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.PrettyBorders
import po.misc.data.pretty_print.parts.RenderOptions
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.presets.RendererPresets
import po.misc.data.pretty_print.presets.RowPresets
import po.misc.data.strings.stringify
import po.misc.data.styles.SpecialChars
import po.misc.io.captureOutput
import po.misc.reflection.NameValuePair
import po.misc.types.castOrThrow
import kotlin.reflect.KProperty1

/**
 * Represents a row of formatted cells that can render an ordered list of values.
 *
 * Typical usage:
 * ```
 * val row = PrettyRow(
 *     PrettyCell(10, KeyPreset),
 *     PrettyCell(20, ValuePreset)
 * )
 * println(row.render(listOf("Name", "John Doe")))
 * ```
 *
 * Extra values exceeding the number of cells are appended after the row
 * using [outsideCellSeparator].
 */
class PrettyRow(
    private var initialCells: List<PrettyCellBase<*>>,
    val separator: String = " | ",
): TraceableContext{
    enum class Orientation{ Horizontal, Vertical }
    enum class RendererOperation{ Render, RenderCell }

    constructor(vararg cells: PrettyCellBase<*>):this(cells.toList()){
        SpecialChars.CELL_SEPARATOR
    }
    constructor(separator: String = " | ", vararg cells: PrettyCellBase<*>):this(cells.toList(), separator)
    constructor(container: PrettyDataContainer):this(container.prettyCells, SpecialChars.CELL_SEPARATOR){
        cells = container.prettyCells
    }

    var defaults: RenderDefaults = Console220
    var outsideCellSeparator: String = " : "

    var options: RowOptions = RowOptions(Console220)
    val rowBorders : PrettyBorders = PrettyBorders()

    var cells: List<PrettyCellBase<*>>
        internal set

    val journal : LogJournal<RendererOperation> = LogJournal(this, RendererOperation.Render)

    init {
        cells = computeFormattedCells()
    }

    private fun computeFormattedCells(): List<PrettyCellBase<*>> {
        val copied = initialCells.map { cell ->
            val cellCopy =  when(cell){
                is PrettyCell -> PrettyCell(cell.width, cell.align)
                is KeyedCell -> KeyedCell(cell.width, cell.cellName)
                is ComputedCell<*> -> ComputedCell<Any>(cell.width,  null)
                is StaticCell -> StaticCell(cell.content, cell.width)
            }
            PrettyCellBase.copyKeyParams(cell, cellCopy)
        }
        return copied
    }
    private fun selectOption(index: Int,  usePlain: Boolean = false): RenderOptions {
        val lastIndex = cells.size - 1
        val options = when {
            index == 0 && cells.size == 1 -> RenderOptions(usePlain, renderLeftBorder = false, false).assignParameters(this)
            index == 0 && cells.size > 1 -> RenderOptions(usePlain, renderLeftBorder = false, true).assignParameters(this)

            index == lastIndex && cells.size == 1 ->  RenderOptions(usePlain, renderLeftBorder = false, false).assignParameters(this)
            index == lastIndex && cells.size > 1 -> RenderOptions(usePlain, renderLeftBorder = false, false).assignParameters(this)

            cells.size > 1 -> RenderOptions(usePlain, renderLeftBorder = false, true).assignParameters(this)
            cells.size <= 1 -> RenderOptions(usePlain, renderLeftBorder = true, true).assignParameters(this)

            else -> RenderOptions(usePlain, renderLeftBorder = true, true).assignParameters(this)
        }
        return options
    }

    private fun runListRenderLessCells(optionsToUse: RowOptions, list: List<Any>, stingBuilder: StringBuilder){
        val cellsCount = cells.size
        val noBordersOption = RenderOptions(renderLeftBorder = false, renderRightBorder = false).assignFinalize(this)
        for (i in 0 until cellsCount) {
            val options = selectOption(i)
            val listItem = list[i]
            if(optionsToUse.orientation == Orientation.Horizontal){
                val value = renderCell(listItem, cells[i], options)
                stingBuilder.append(value)
            }else{
                val value = renderCell(listItem, cells[i], noBordersOption)
                stingBuilder.appendLine(value)
            }
        }
        val tailString =  list.drop(cellsCount).joinToString(" ")

        cells.lastOrNull()?.let {
            val tailResult = it.render(tailString)
            stingBuilder.append(tailResult)
        }
    }
    private fun runListRenderCellsMoreOrEqual(optionsToUse: RowOptions, list: List<Any>, stingBuilder: StringBuilder) {

        val noBordersOption = RenderOptions(renderLeftBorder = false, renderRightBorder = false).assignFinalize(this)
        list.forEachIndexed { index, element->
            val options = selectOption(index)
            val selectedCell = cells[index]
            if(optionsToUse.orientation == Orientation.Horizontal){
                val rendered = renderCell(element, selectedCell, options)
                stingBuilder.append(rendered)
            }else{
                val rendered = renderCell(element, selectedCell, noBordersOption)
                stingBuilder.appendLine(rendered)
            }
        }
    }

    internal fun <T: Any> renderKeyed(receiver: T, cell: KeyedCell, options: RenderOptions): String{
        val castedProperty =  cell.property.castOrThrow<KProperty1<T, *>>()
        val formatted = castedProperty.get(receiver).stringify()
        val renderResult = cell.render(formatted, options)
        return renderResult
    }

    internal fun <T: Any> renderPretty(receiver: T, prettyCell: PrettyCell): String{
        val result = prettyCell.postfix?:""
        return result
    }

    internal fun <T: Any>  renderStatic(receiver: T,  cell: StaticCell, options: RenderOptions): String{
        journal.addRecord(RendererOperation.RenderCell, "Static cell with options: $options")
        val formatted = cell.changeContent(receiver).stringify()
        val renderResult = cell.render(formatted, options)
        return renderResult
    }

    internal fun <T: Any> renderComputed(receiver: T, cell: ComputedCell<*>, options: RenderOptions): String{
        journal.addRecord(RendererOperation.RenderCell, "Computed cell with options: [$options]")
        val casted = cell.castOrThrow<ComputedCell<T>>()
        val newReceiver = casted.property?.get(receiver)
        if(newReceiver != null){
            val result = casted.lambda?.invoke(casted, newReceiver)
            val formatted = result.stringify()
            val renderResult = cell.render(formatted, options)
            return renderResult
        }
        return ""
    }

    internal fun <T: Any> renderCell(receiver: T, cell: PrettyCellBase<*>, options: RenderOptions): String{
        return  when(cell){
            is ComputedCell<*> -> renderComputed(receiver, cell, options)
            is KeyedCell -> renderKeyed(receiver, cell, options)
            is StaticCell-> renderStatic(receiver, cell, options)
            is PrettyCell -> renderPretty(receiver, cell)
        }
    }

    internal fun <T: Any> runRender(receiver: T, rowPreset: RowPresets? = null): String {
        val useOptions = rowPreset?.toOptions(defaults)?:options
        val cellCount = cells.size
        journal.addRecord("Cells count $cellCount")
        val result = StringBuilder()
        if(cellCount > 0){
            runListRenderCellsMoreOrEqual(useOptions, receiver.asList(), result)
        }
        return result.toString()
    }
    internal fun <T: Any> runListRender(list: List<T>, rowPreset: RowPresets? = null): String {
        val rowOptions = rowPreset?.toOptions(defaults)
        val optionsToUse = rowOptions?:options
        val cellsCount = cells.size
        journal.addRecord("Cells count $cellsCount")
        val listSize = list.size
        val result = StringBuilder()
        when {
            cellsCount < listSize -> runListRenderLessCells(optionsToUse, list, result)
            cellsCount >= listSize -> runListRenderCellsMoreOrEqual(optionsToUse, list, result)
        }
        return result.toString()
    }

    fun <T: Any> render(receiver: T, rowPreset: RowPresets? = null): String = runRender(receiver, rowPreset)

    fun render(vararg values: Any, rowPreset: RowPresets? = null): String{
        val valuesList = values.toList()
        return runListRender(valuesList, rowPreset)
    }
    fun render(values: List<String>, rowPreset: RowPresets? = null): String {
        return runListRender(values, rowPreset)
    }
    fun render(nameValuePair: NameValuePair): String = runListRender(listOf(nameValuePair.name, nameValuePair.value))

    fun setCells(newCells: List<PrettyCellBase<*>>){
        initialCells = newCells
        computeFormattedCells()
    }
    fun applyPreset(preset: RowPresets): PrettyRow{
        options = preset.toOptions()
        return this
    }

}