package po.misc.data.pretty_print.rows

import po.misc.collections.indexed.Indexed
import po.misc.context.tracable.TraceableContext
import po.misc.counters.LogJournal
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.parts.Console220
import po.misc.data.pretty_print.parts.RenderDefaults
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.PrettyBorders
import po.misc.data.pretty_print.parts.RenderOptions
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.presets.RowPresets
import po.misc.data.pretty_print.rows.TransitionRow
import po.misc.data.strings.stringify
import po.misc.reflection.NameValuePair
import po.misc.reflection.Readonly
import po.misc.reflection.getBrutForced
import po.misc.reflection.resolveTypedProperty
import po.misc.types.castOrThrow
import po.misc.types.getOrThrow
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
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
abstract class PrettyRowBase(
    useId: Enum<*>? = null,
    private var initialCells: List<PrettyCellBase<*>>,
): PrettyDataContainer, TraceableContext, Indexed {

    enum class RendererOperation{ Render, RenderCell }

    internal val cellsBacking: MutableList<PrettyCellBase<*>> = mutableListOf()

    var myIndex: Int = 0
        private set
    var ofCount: Int = 0
        private set

    val isFirst: Boolean get() = myIndex == 0
    val isLast: Boolean get() = myIndex == ofCount - 1

    var defaults: RenderDefaults = Console220
    var options: RowOptions = RowOptions(Console220)
    val rowBorders : PrettyBorders = PrettyBorders()
    override var id : Enum<*>?
        get() = options.id
        set(value) {
            value?.let { options.id = it }
        }

    override val cells : List<PrettyCellBase<*>> get() = cellsBacking
    val journal : LogJournal<RendererOperation> = LogJournal(this, RendererOperation.Render)

    init {
        if(useId != null){
            options.id = useId
        }
        setCells(initialCells)
    }

    private fun selectOption(cell: PrettyCellBase<*>, rowOption: RowOptions): RenderOptions{
        val lastIndex = cells.size - 1
        val index = cell.index
        val orientation = rowOption.orientation
        val usePlain = false

        if(orientation == Orientation.Vertical){
           return RenderOptions(orientation =  orientation, renderLeftBorder = false, renderRightBorder = false).assignFinalize(this)
        }
        val options = when {
            index == 0 && cells.size == 1 -> RenderOptions(orientation, usePlain, renderLeftBorder = false, false).assignParameters(this)
            index == 0 && cells.size > 1 -> RenderOptions(orientation, usePlain, renderLeftBorder = false, true).assignParameters(this)

            index == lastIndex && cells.size == 1 ->  RenderOptions(orientation, usePlain, renderLeftBorder = false, false).assignParameters(this)
            index == lastIndex && cells.size > 1 -> RenderOptions(orientation,usePlain, renderLeftBorder = false, false).assignParameters(this)

            cells.size > 1 -> RenderOptions(orientation, usePlain, renderLeftBorder = false, true).assignParameters(this)
            cells.size <= 1 -> RenderOptions(orientation, usePlain, renderLeftBorder = true, true).assignParameters(this)

            else -> RenderOptions(orientation, usePlain, renderLeftBorder = true, true).assignParameters(this)
        }
        return options
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
        val noBordersOption = RenderOptions(orientation =  rowOptions.orientation, renderLeftBorder = false, renderRightBorder = false).assignFinalize(this)
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

    private fun <T: Any> renderKeyed(receiver: T, cell: KeyedCell, options: RenderOptions): String{
        val castedProperty =  cell.property.castOrThrow<KProperty1<T, *>>()
        val formatted = castedProperty.get(receiver).stringify()
        val renderResult = cell.render(formatted, options)
        return renderResult
    }
    private fun <T: Any> renderPretty(receiver: T, prettyCell: PrettyCell): String{
        val result = prettyCell.postfix?:""
        return result
    }
    private fun <T: Any> renderStatic(receiver: T,  cell: StaticCell, options: RenderOptions): String{
        journal.addRecord(RendererOperation.RenderCell, "Static cell with options: $options")
        val renderResult = if(cell.lockContent){
            cell.render(options)
        }else{
            val formatted = receiver.stringify()
            cell.render(formatted, options)
        }
        return renderResult
    }
    private fun <T: Any> renderComputed(receiver: T, cell: ComputedCell<*>, options: RenderOptions): String{
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
            is StaticCell->  renderStatic(receiver, cell, options)
            is PrettyCell -> renderPretty(receiver, cell)
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
        journal.addRecord("Cells count $cellCount")
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
    fun <T: Any> render(receiver: T, rowPreset: RowPresets? = null): String{
        return if(rowPreset != null){
            runRender(receiver, rowPreset.toOptions())
        }else{
            runRender(receiver, null)
        }
    }

    fun render(value: Any,  vararg values: Any, rowPreset: RowPresets? = null): String{
        val valuesList = buildList {
            add(value)
            addAll(values.toList())
        }
        return if(rowPreset != null){
            runListRender(valuesList,  rowPreset.toOptions())
        }else{
            runListRender(valuesList)
        }
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
    fun applyPreset(preset: RowPresets): PrettyRowBase{
        options = preset.toOptions()
        return this
    }

    override fun setIndex(index: Int, ofSize: Int) {
        myIndex = index
        ofCount = ofSize
    }
}

/**
 * A standard pretty-printing row.
 *
 * This row uses the receiver object passed during rendering and renders its
 * cells directly against that object (no nesting or list expansion).
 *
 * @param cells the cells that form this row
 * @param id optional identifier for selective rendering
 */
class PrettyRow(
    cells: List<PrettyCellBase<*>>,
    id: Enum<*>? = null,
): PrettyRowBase(id, cells){

    constructor(vararg cells: PrettyCellBase<*>,  id: Enum<*>? = null):this(cells.toList(), id  = id)
    constructor(container: PrettyDataContainer):this(container.cells, id =  container.id)

    companion object{

        @PublishedApi
        internal fun <T : Any> buildRow(
            token: TypeToken<T>,
            rowOptions: RowOptions? = null,
            builder: CellContainer<T>.() -> Unit
        ): PrettyRow {
            val constructor = CellContainer<T>(token)
            builder.invoke(constructor)
            val realRow = PrettyRow(constructor)
            if (rowOptions != null) {
                realRow.options = rowOptions
            }
            return realRow
        }

        @PublishedApi
        internal fun <T : Any> buildRowForContext(
            receiver: T,
            token: TypeToken<T>,
            rowOptions: RowOptions? = null,
            builder: CellReceiverContainer<T>.(T) -> Unit
        ): PrettyRow {
            val constructor = CellReceiverContainer<T>(receiver, token)
            builder.invoke(constructor, receiver)
            val realRow = PrettyRow(constructor)
            if (rowOptions != null) {
                realRow.options = rowOptions
            }
            return realRow
        }
    }
}






