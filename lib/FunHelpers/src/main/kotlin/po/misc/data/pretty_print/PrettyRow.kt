package po.misc.data.pretty_print

import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
import po.misc.context.tracable.TraceableContext
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.ReceiverAwareCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RenderDefaults
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.data.pretty_print.parts.rows.RowParams
import po.misc.data.pretty_print.rows.RowContainerBase
import po.misc.data.strings.stringify
import po.misc.data.styles.SpecialChars
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import po.misc.types.token.safeCast

/**
 * A standard pretty-printing row.
 *
 * This row uses the receiver object passed during rendering and renders its
 * cells directly against that object (no nesting or list expansion).
 *
 * @param cells the cells that form this row
 * @param id optional identifier for selective rendering
 */
class PrettyRow<T: Any>(
    val typeToken: TypeToken<T>,
    override var options: RowOptions = RowOptions(RenderDefaults.Console220),
    initialCells: List<PrettyCellBase> = emptyList(),
): RenderableElement<T>, TraceableContext {

    constructor(container: RowContainerBase<T, T>):this(container.type, container.options,  container.cells)

    private val cellsBacking: MutableList<PrettyCellBase> = mutableListOf()

    override var id : Enum<*>?
        get() = options.rowId
        set(value) {
            options.rowId = value
        }

    var orientation: Orientation
        get() =  options.orientation
        set(value) {
            options.orientation = value
        }

    val prettyCells: List<PrettyCell> get() = cellsBacking.filterIsInstance<PrettyCell>()
    val staticCells: List<StaticCell> get() = cellsBacking.filterIsInstance<StaticCell>()
    val keyedCells: List<KeyedCell<*>> get() = cellsBacking.filterIsInstance<KeyedCell<*>>()
    val computedCells: List<ComputedCell<*, *>> get() = cellsBacking.filterIsInstance<ComputedCell<*, *>>()
    val cells : List<PrettyCellBase> get() = cellsBacking

    val size: Int get() = cells.size
    val rowMaxWidth: Int get()  = cells.maxOf { it.cellOptions.width }

    val beforeRowRender: Signal<RowParams<T>, Unit> = signalOf<RowParams<T>, Unit>()
    val afterRowRender: Signal<RowParams<T>, Unit> = signalOf<RowParams<T>, Unit>()

    init {
        setCells(initialCells)
    }

    private fun renderSelection(receiver:T, cell: PrettyCellBase, cellOptions: CellOptions?): String {
        return when (cell) {
            is ReceiverAwareCell<*> -> {
                val casted = cell.safeCast<ReceiverAwareCell<T>>()
                casted?.render(receiver, cellOptions) ?: ""
            }
            is StaticCell -> cell.render(cellOptions)
            else -> cell.render(receiver.stringify().formatedText, cellOptions)
        }
    }

    /**
     * Renders this row using a typed receiver.
     *
     * If [opts] is provided, it will be converted into [CellOptions]
     * and applied during rendering.
     *
     * @param receiver the object used to populate the row's cells
     * @param opts optional preset controlling styling and layout
     * @return the rendered row text
     */
    fun render(
        receiver: T,
        opts: CommonRowOptions? = null,
        optionsBuilder: (RowOptions.()-> Unit)? = null
    ): String {

        val resultList = mutableListOf<String>()
        val useOptions = PrettyHelper.toRowOptions(options, opts)
        optionsBuilder?.invoke(useOptions)
        orientation = useOptions.orientation

        val cellsToRender = cells
        val cellCount = cellsToRender.size
        val cellsToTake = (cellCount - 1).coerceAtLeast(0)
        beforeRowRender.trigger(RowParams(this, useOptions))
        cellsToRender.take(cellsToTake).forEach {cell->
            val render =  renderSelection(receiver, cell, null)
            resultList.add(render)
        }
        cellsToRender.lastOrNull()?.let {lastCell->
            val render = renderSelection(receiver, lastCell, null)
            resultList.add(render)
        }
        val render = if(orientation == Orientation.Vertical){
            resultList.joinToString(separator = SpecialChars.NEW_LINE)
        }else{
            resultList.joinToString(separator = SpecialChars.EMPTY)
        }
        afterRowRender.trigger(RowParams(this, useOptions, render))
        return render
    }
    override fun renderOnHost(host: T, opts: CommonRowOptions?): String {
        return render(host, opts)
    }

    fun render(
        receiverList: List<T>,
        opts: CommonRowOptions? = null,
        optionsBuilder: (RowOptions.()-> Unit)? = null
    ): String {
        val useOptions = PrettyHelper.toRowOptions(opts, options)
        optionsBuilder?.invoke(useOptions)
        orientation = useOptions.orientation

        val resultList =  receiverList.map { render(it, null) }
        return resultList.joinToString(separator = SpecialChars.NEW_LINE)
    }

    /**
     * Renders this row from a preformatted list of string values.
     *
     * Useful for manual rendering or special cases where reflective access
     * is not desired.
     *
     * @param values the formatted values to render in this row
     * @param opts optional preset controlling styling and layout
     * @return rendered row text
     */
    fun render(values: List<Any>, opts: CommonRowOptions? = null): String {
        fun renderPrettyCell(receiver: Any, cell: PrettyCell, cellOptions: CellOptions?): String {
            return cell.render(receiver, cellOptions)
        }
        val resultList = mutableListOf<String>()
        val options = PrettyHelper.toOptionsOrNull(opts)

        val valuesList = values.toList()
        valuesList.forEach {value->
            val casted = value.safeCast(typeToken)
            if(casted != null){
                val render = render(casted, opts)
                resultList.add(render)
            }else{
                val prettyCells = cells.filterIsInstance<PrettyCell>()
                prettyCells.forEach {cell->
                    val render = renderPrettyCell(value, cell, options)
                    resultList.add(render)
                }
            }
        }
        return  resultList.joinToString(separator = SpecialChars.NEW_LINE)
    }

    fun renderAny(vararg values: Any, rowOptions: CommonRowOptions? = null): String{
        return render(values.toList(), rowOptions)
    }
    fun applyOptions(opt: CommonRowOptions?): PrettyRow<T>{
        if(opt != null){
            options = when(opt){
                is RowOptions -> opt
                is RowPresets -> PrettyHelper.Companion.toRowOptions(opt)
            }
        }
        return this
    }

    fun setCells(newCells: List<PrettyCellBase>){
        if(newCells.isNotEmpty()){
            cellsBacking.clear()
            newCells.forEachIndexed { index, cell->
                cell.row = this
                cell.index = index
                cellsBacking.add(cell)
            }
        }
    }
    fun setCells(cell: PrettyCellBase, vararg newCells: PrettyCellBase){
        val newList = buildList {
            add(cell)
            addAll(newCells.toList())
        }
        setCells(newList)
    }

    fun beforeRowRender(callback: (RowParams<T>) -> Unit){
        beforeRowRender.onSignal(callback)
    }

    override fun toString(): String {
        val static = "Static: ${staticCells.size}"
        val keyed = "Keyed: ${keyedCells.size}"
        val computed = "Computed: ${computedCells.size}"
        val pretty = "Pretty: ${prettyCells.size}"
        return "PrettyRow[Total: ${cells.size}, $static, $keyed, $computed, $pretty]"
    }

    companion object {
        operator fun invoke(cells: List<PrettyCellBase>, options: RowOptions? = null):PrettyRow<String>{
            val  typeToken: TypeToken<String> = TypeToken.Companion.create()
            val row = PrettyRow(typeToken, initialCells = cells)
            return  row.applyOptions(options)
        }
        operator fun invoke(vararg cells: PrettyCellBase, options: RowOptions? = null):PrettyRow<String>{
            return invoke(cells.toList(), options)
        }
        inline operator fun <reified T: Any> invoke(vararg cells: KeyedCell<T>, options: RowOptions? = null):PrettyRow<T>{
            val  typeToken: TypeToken<T> = TypeToken.Companion.create()
            val row = PrettyRow(typeToken, initialCells = cells.toList())
            return row.applyOptions(options)
        }
    }
}