package po.misc.data.pretty_print.rows

import po.misc.collections.indexed.Indexed
import po.misc.context.tracable.TraceableContext
import po.misc.counters.LogJournal
import po.misc.counters.records.LogJournalEntry
import po.misc.data.output.output
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.parts.Console220
import po.misc.data.pretty_print.parts.RenderDefaults
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.CellRender
import po.misc.data.pretty_print.parts.CommonRenderOptions
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.PrettyBorders
import po.misc.data.pretty_print.parts.ReceiverLoader
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.data.pretty_print.parts.RowRender
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.reflection.NameValuePair
import po.misc.types.castOrThrow
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1

/**
 * Base class for all pretty-printing row types.
 *
 * A row represents a single logical rendering block that contains one or more
 * [PrettyCellBase] elements. Each row knows:
 *
 * - its identification tag ([id]) for selective rendering
 * - its initial cell structure
 * - its ability to render itself against:
 *      - an object receiver (via reflection)
 *      - a precomputed list of string values
 *
 * Subclasses provide specialized behavior:
 * - [PrettyRow] - standard row
 * - [TransitionRow] - row with a nested receiver derived from the parent receiver
 * - [ListContainingRow] - row that renders once per element of a collection
 */
abstract class PrettyRowBase<T: Any>(
    val  typeToken: TypeToken<T>,
    private var initialCells: List<PrettyCellBase<*>>,
    var options: RowOptions = RowOptions(Console220),
):  TraceableContext, Indexed{

    enum class RendererOperation{ Render, RenderCell }

    var myIndex: Int = 0
        private set
    var ofCount: Int = 0
        private set

    val isFirst: Boolean get() = myIndex == 0
    val isLast: Boolean get() = myIndex == ofCount - 1

    var defaults: RenderDefaults = Console220
    val rowBorders : PrettyBorders = PrettyBorders()

    var id : Enum<*>?
        get() = options.id
        set(value) {
            options.id = value
        }

    internal val cellsBacking: MutableList<PrettyCellBase<*>> = mutableListOf()
    val cells : List<PrettyCellBase<*>> get() = cellsBacking

    val prettyCells: List<PrettyCell> get() = cellsBacking.filterIsInstance<PrettyCell>()
    val staticCells: List<StaticCell> get() = cellsBacking.filterIsInstance<StaticCell>()
    val keyedCells: List<KeyedCell> get() = cellsBacking.filterIsInstance<KeyedCell>()
    val computedCells: List<ComputedCell<*>> get() = cellsBacking.filterIsInstance<ComputedCell<*>>()

    val journal : LogJournal = LogJournal(this)

    init {
        setCells(initialCells)
    }

    private fun selectOption(cell: PrettyCellBase<*>, rowOption: RowOptions): CellRender{
        val lastIndex = cells.size - 1
        val index = cell.index
        val orientation = rowOption.orientation
        val usePlain = false

        if(orientation == Orientation.Vertical){
           return CellRender(orientation =  orientation, renderLeftBorder = false, renderRightBorder = false).assignFinalize(this)
        }
        val options = when {
            index == 0 && cells.size == 1 -> CellRender(orientation, usePlain, renderLeftBorder = false, false).assignParameters(this)
            index == 0 && cells.size > 1 -> CellRender(orientation, usePlain, renderLeftBorder = false, true).assignParameters(this)

            index == lastIndex && cells.size == 1 ->  CellRender(orientation, usePlain, renderLeftBorder = false, false).assignParameters(this)
            index == lastIndex && cells.size > 1 -> CellRender(orientation,usePlain, renderLeftBorder = false, false).assignParameters(this)

            cells.size > 1 -> CellRender(orientation, usePlain, renderLeftBorder = false, true).assignParameters(this)
            cells.size <= 1 -> CellRender(orientation, usePlain, renderLeftBorder = true, true).assignParameters(this)

            else -> CellRender(orientation, usePlain, renderLeftBorder = true, true).assignParameters(this)
        }
        return options
    }

    private fun selectOption(cell: PrettyCellBase<*>, render: CommonRenderOptions): CellRender{
        val lastIndex = cells.size - 1
        val index = cell.index
        val orientation = render.orientation
        val usePlain = false

        if(orientation == Orientation.Vertical){
            return CellRender(orientation =  orientation, renderLeftBorder = false, renderRightBorder = false).assignFinalize(this)
        }
        val options = when {
            index == 0 && cells.size == 1 -> CellRender(orientation, usePlain, renderLeftBorder = false, false).assignParameters(this)
            index == 0 && cells.size > 1 -> CellRender(orientation, usePlain, renderLeftBorder = false, true).assignParameters(this)

            index == lastIndex && cells.size == 1 ->  CellRender(orientation, usePlain, renderLeftBorder = false, false).assignParameters(this)
            index == lastIndex && cells.size > 1 -> CellRender(orientation,usePlain, renderLeftBorder = false, false).assignParameters(this)

            cells.size > 1 -> CellRender(orientation, usePlain, renderLeftBorder = false, true).assignParameters(this)
            cells.size <= 1 -> CellRender(orientation, usePlain, renderLeftBorder = true, true).assignParameters(this)

            else -> CellRender(orientation, usePlain, renderLeftBorder = true, true).assignParameters(this)
        }
        return options
    }

    private fun noBorderOption(orientation: Orientation):CellRender{
       return CellRender(orientation, renderLeftBorder = false, renderRightBorder = false).assignFinalize(this)
    }

    private fun <T: Any> renderKeyed(receiver: T, cell: KeyedCell, options: CellRender): String{
        val castedProperty =  cell.property.castOrThrow<KProperty1<T, *>>()
        val formatted = castedProperty.get(receiver).stringify()
        val renderResult = cell.render(formatted, options)
        return renderResult
    }
    private fun <T: Any> renderPretty(receiver: T, prettyCell: PrettyCell): String{
        val result = prettyCell.postfix?:""
        return result
    }
    private fun <T: Any> renderStatic(receiver: T,  cell: StaticCell, options: CellRender): String{
        journal.addRecord("Static cell with options: $options")
        val renderResult = if(cell.lockContent){
            cell.render(options)
        }else{
            val formatted = receiver.stringify()
            cell.render(formatted, options)
        }
        return renderResult
    }
    private fun <T: Any> renderComputed(receiver: T, cell: ComputedCell<*>, options: CellRender): String{
        journal.addRecord("Computed cell with options: [$options]")
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
    internal fun <T: Any> renderCell(receiver: T, cell: PrettyCellBase<*>, options: CellRender): String{
        return  when(cell){
            is ComputedCell<*> -> renderComputed(receiver, cell, options)
            is KeyedCell -> renderKeyed(receiver, cell, options)
            is StaticCell->  renderStatic(receiver, cell, options)
            is PrettyCell -> renderPretty(receiver, cell)
        }
    }

    private fun runListRenderLessCells(rowOptions: RowOptions, list: List<Any>, stingBuilder: StringBuilder){
        val cellsCount = cells.size
        for (i in 0 until cellsCount) {
            val cell = cells[i]
            val listItem = list[i]
            val options = selectOption(cell, rowOptions)
            if(rowOptions.orientation == Orientation.Horizontal){
                val value = renderCell(listItem, cell, options)
                stingBuilder.append(value)
            }else{
                val value = renderCell(listItem, cell, options)
                stingBuilder.appendLine(value)
            }
        }
        val tailString =  list.drop(cellsCount).joinToString(" ")

        cells.lastOrNull()?.let {
            val options = selectOption(it, rowOptions)
            val tailResult = it.render(tailString, options)
            stingBuilder.append(tailResult)
        }
    }


    private fun runListRenderCellsMoreOrEqual(rowOptions: RowOptions, list: List<Any>, stingBuilder: StringBuilder) {
        val noBordersOption = CellRender(orientation =  rowOptions.orientation, renderLeftBorder = false, renderRightBorder = false).assignFinalize(this)
        list.forEachIndexed { index, element->
            val selectedCell = cells[index]

            val options = selectOption(selectedCell, rowOptions)

            if(rowOptions.orientation == Orientation.Horizontal){
                val rendered = renderCell(element, selectedCell, options)
                stingBuilder.append(rendered)
            }else{
                val rendered = renderCell(element, selectedCell, noBordersOption)
                stingBuilder.appendLine(rendered)
            }
        }
    }

    internal fun <T: Any> runRender(
        receiver: T,
        rowOptions: RowOptions?,
        renderNamedOnly: List<Enum<*>> = emptyList()
    ): String {

       val cellsToRender =  if(renderNamedOnly.isNotEmpty()){
            val cellsWithIds = cells.filter { it.options.id == null || it.options.id in renderNamedOnly }
            cellsWithIds
        }else{
            cells
        }

        val useRowOptions = rowOptions?: options
        val cellCount = cellsToRender.size
        val cellsToTake = (cellCount - 1).coerceAtLeast(0)
      //  journal.addRecord("Cells count $cellCount")
        val result = StringBuilder()

        cellsToRender.take(cellsToTake).forEach {cell->
            val options = selectOption(cell, useRowOptions)
            if (useRowOptions.orientation == Orientation.Horizontal) {
                val rendered = renderCell(receiver, cell, options)
                result.append(rendered)
            } else {
                val rendered = renderCell(receiver, cell, options)
                result.appendLine(rendered)
            }
        }
        cellsToRender.lastOrNull()?.let {lastCell->
            val options = selectOption(lastCell, useRowOptions)
            val rendered = renderCell(receiver, lastCell, options)
            result.append(rendered)
        }
        return result.toString()
    }


    internal fun <T: Any> runListRender(list: List<T>, rowOptions: RowOptions? = null): String {
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

    /**
     * Renders this row using a typed receiver.
     *
     * If [rowPreset] is provided, it will be converted into [RowOptions]
     * and applied during rendering.
     *
     * @param receiver the object used to populate the row's cells
     * @param rowPreset optional preset controlling styling and layout
     * @return the rendered row text
     */
    open fun <T: Any> render(receiver: T, rowPreset: RowPresets? = null): String{
        return if(rowPreset != null){
            runRender(receiver, rowPreset.toOptions())
        }else{
            runRender(receiver, null)
        }
    }


    private fun <T: Any> lessCells(list: List<T>, renderOptions: CommonRenderOptions): List<String>{
        val resultList = mutableListOf<String>()
        val cellsCount = cells.size
        for (i in 0 until cellsCount) {
            val cell = cells[i]
            val options = selectOption(cell, renderOptions)
            val listItem = list[i]

            if(options.orientation == Orientation.Horizontal){
                val cellRender = renderCell(listItem, cell, options)
                resultList.add(cellRender)
            }else{
                val cellRender = renderCell(listItem, cell, options)
                resultList.add(cellRender)
            }
        }
        val tailString =  list.drop(cellsCount).joinToString(" ")

        cells.lastOrNull()?.let {
            val options = selectOption(it, renderOptions)
            val tailCellRender = it.render(tailString, options)
            resultList.add(tailCellRender)
        }
        return resultList
    }

    private fun  <T: Any> moreOrEqual(list: List<T>, renderOptions: CommonRenderOptions): List<String> {
        val resultList = mutableListOf<String>()
       // val noBordersOption = CellRender(orientation =  rowOptions.orientation, renderLeftBorder = false, renderRightBorder = false).assignFinalize(this)
        list.forEachIndexed { index, element->
            val selectedCell = cells[index]
            val options = selectOption(selectedCell, renderOptions)
            if(options.orientation == Orientation.Horizontal){
                val cellRender = renderCell(element, selectedCell, options)
                resultList.add(cellRender)
            }else{
                val noBorder = noBorderOption(renderOptions.orientation)
                val cellRender = renderCell(element, selectedCell, noBorder)
                resultList.add(cellRender)
            }
        }
        return resultList
    }


    fun <T: Any> renderList(list: List<T>, renderOptions: CommonRenderOptions): String {
        val cellsCount = cells.size
        journal.addRecord("Cells count $cellsCount")
        val listSize = list.size
      // val result = StringBuilder()
        val renderedList = when {
            cellsCount < listSize -> lessCells(list, renderOptions)
            cellsCount >= listSize ->{
                moreOrEqual(list, renderOptions)
                //runListRenderCellsMoreOrEqual(optionsToUse, list, result)
            }
            else-> emptyList()
        }
        return renderedList.joinToString(separator = SpecialChars.NEW_LINE)
    }

    fun <T: Any> renderAsList(receiver: T, render: RowRender): List<String> {
        val resultList = mutableListOf<String>()
        val cellsToRender = cells
        val cellCount = cellsToRender.size
        val cellsToTake = (cellCount - 1).coerceAtLeast(0)

        cellsToRender.take(cellsToTake).forEach {cell->
            val options = selectOption(cell, render)
            if (render.orientation == Orientation.Horizontal) {
                val rendered = renderCell(receiver, cell, options)
                resultList.add(rendered)
            } else {
                val rendered = renderCell(receiver, cell, options)
                resultList.add(rendered)
            }
        }
        cellsToRender.lastOrNull()?.let {lastCell->
            val options = selectOption(lastCell, render)
            val rendered = renderCell(receiver, lastCell, options)
            resultList.add(rendered)
        }
        return resultList
    }
    open fun <T: Any> render(receiver: T, render: RowRender):String{
        return  renderAsList(receiver, render).joinToString(separator = SpecialChars.NEW_LINE)
    }

    fun render(value: Any,  vararg values: Any, prettyPreset: PrettyPresets? = null): String{
       val renderOption = if(prettyPreset != null){
            CellRender(options.orientation,  prettyPreset.align)
        }else{
            CellRender()
        }
        val valuesList = buildList {
            add(value)
            addAll(values.toList())
        }
        return  renderList(valuesList, renderOption)
    }

    /**
     * Renders this row from a preformatted list of string values.
     *
     * Useful for manual rendering or special cases where reflective access
     * is not desired.
     *
     * @param values the formatted values to render in this row
     * @param rowPreset optional preset controlling styling and layout
     * @return rendered row text
     */
    fun render(values: List<String>, rowPreset: RowPresets? = null): String {

        return if(rowPreset != null){
            runListRender(values,  rowPreset.toOptions())
        }else{
            runListRender(values)
        }
    }

    fun render(nameValuePair: NameValuePair): String = runListRender(listOf(nameValuePair.name, nameValuePair.value))

    fun setCells(newCells: List<PrettyCellBase<*>>){
        cellsBacking.clear()
        newCells.forEachIndexed { index, cell->
            cell.index = index
            cellsBacking.add(cell)
        }
    }
    fun setCells(cell:  PrettyCellBase<*>,  vararg newCells: PrettyCellBase<*>): Unit{
        val newList = buildList {
            add(cell)
            addAll(newCells.toList())
        }
        setCells(newList)
    }
    fun applyPreset(preset: RowPresets): PrettyRowBase<T>{
        options = preset.toOptions()
        return this
    }

    fun setRowOptions(newOptions: RowOptions?):PrettyRowBase<T>{
        if(newOptions!= null){
            options = newOptions
        }
        return this
    }

    override fun setIndex(index: Int, ofSize: Int) {
        myIndex = index
        ofCount = ofSize
    }
}









