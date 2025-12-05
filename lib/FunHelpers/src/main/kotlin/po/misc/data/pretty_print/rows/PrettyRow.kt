package po.misc.data.pretty_print.rows

import po.misc.collections.asList
import po.misc.collections.indexed.Indexed
import po.misc.context.tracable.TraceableContext
import po.misc.counters.LogJournal
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.ReceiverAwareCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.Console220
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.data.strings.stringify
import po.misc.data.styles.SpecialChars
import po.misc.types.safeCast
import po.misc.types.token.TypeToken


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
    private var initialCells: List<PrettyCellBase<*>>,
    var options: RowOptions = RowOptions(Console220),
): RenderableElement<T, T>, TraceableContext, Indexed{

    constructor(container: CellContainerBase<T>):this(container.typeToken,  container.cells,  container.options)

    var id : Enum<*>?
        get() = options.id
        set(value) {
            options.id = value
        }

    override val ids: List<Enum<*>> get() = options.id?.asList()?:emptyList()


    internal val cellsBacking: MutableList<PrettyCellBase<*>> = mutableListOf()
    val cells : List<PrettyCellBase<*>> get() = cellsBacking

    var myIndex: Int = 0
        private set
    var ofCount: Int = 0
        private set

    val isFirst: Boolean get() = myIndex == 0
    val isLast: Boolean get() = myIndex == ofCount - 1


    val prettyCells: List<PrettyCell> get() = cellsBacking.filterIsInstance<PrettyCell>()
    val staticCells: List<StaticCell> get() = cellsBacking.filterIsInstance<StaticCell>()
    val keyedCells: List<KeyedCell<*>> get() = cellsBacking.filterIsInstance<KeyedCell<*>>()
    val computedCells: List<ComputedCell<*, *>> get() = cellsBacking.filterIsInstance<ComputedCell<*, *>>()

    val journal : LogJournal = LogJournal(this)

    init {
        setCells(initialCells)
    }

    override fun toString(): String {
        val prettyCells = "PrettyCells: ${prettyCells.size}"
        val keyedCells = "PrettyCells: ${keyedCells.size}"
        val staticCells = "PrettyCells: ${staticCells.size}"
        return "PrettyRow[Total: ${cells.size},$prettyCells,$staticCells, $keyedCells ]"
    }

    fun renderAsList(receiver: T, rowOptions: CommonRowOptions?): List<String> {

        fun renderSelection(receiver:T,  cell: PrettyCellBase<*>, cellOptions: CellOptions?): String {
            return when (cell) {
                is ReceiverAwareCell<*> -> {
                    val casted = cell.safeCast<ReceiverAwareCell<T>>()
                    casted?.render(receiver, cellOptions) ?: ""
                }
                is StaticCell -> cell.render(cellOptions)
                else -> cell.render(receiver.stringify().formatedText, cellOptions)
            }
        }
        val resultList = mutableListOf<String>()
        val options = PrettyHelper.toCellOptionsOrNull(rowOptions)
        val cellsToRender = cells
        val cellCount = cellsToRender.size
        val cellsToTake = (cellCount - 1).coerceAtLeast(0)

        cellsToRender.take(cellsToTake).forEach {cell->
            val render =   renderSelection(receiver, cell, options)
            resultList.add(render)
        }
        cellsToRender.lastOrNull()?.let {lastCell->
            val render =   renderSelection(receiver, lastCell, options)
            resultList.add(render)
        }
        return resultList
    }

    /**
     * Renders this row using a typed receiver.
     *
     * If [commonRowOptions] is provided, it will be converted into [CellOptions]
     * and applied during rendering.
     *
     * @param receiver the object used to populate the row's cells
     * @param commonRowOptions optional preset controlling styling and layout
     * @return the rendered row text
     */
    fun render(receiver: T, commonRowOptions: CommonRowOptions? = null):String{
        val renderedList = renderAsList(receiver, commonRowOptions)
        return renderedList.joinToString(separator = SpecialChars.EMPTY)
    }

    override fun renderOnHost(host: T, opts: CommonRowOptions?): String {
        return render(host, opts)
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
    fun render(values: List<Any>, rowOptions: CommonRowOptions? = null): String {

        fun renderPrettyCell(receiver: Any, cell: PrettyCell, cellOptions: CellOptions?): String {
            return cell.render(receiver, cellOptions)
        }

        val resultList = mutableListOf<String>()
        val options = PrettyHelper.toCellOptionsOrNull(rowOptions)

        val valuesList = values.toList()
        valuesList.forEach {value->
            if(value::class == typeToken.kClass){
                val render = render(value as T, rowOptions)
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

    fun applyPreset(preset: RowPresets): PrettyRow<T>{
        options = preset.toOptions()
        return this
    }

    fun setRowOptions(newOptions: RowOptions?):PrettyRow<T>{
        if(newOptions!= null){
            options = newOptions
        }
        return this
    }

    override fun setIndex(index: Int, ofSize: Int) {
        myIndex = index
        ofCount = ofSize
    }

    companion object{
        operator fun invoke(vararg cells: PrettyCellBase<*>):PrettyRow<String>{
            val  typeToken: TypeToken<String> = TypeToken.create()
            return PrettyRow(typeToken, cells.toList())
        }

        operator fun invoke(cells: List<PrettyCellBase<*>>):PrettyRow<String>{
            val  typeToken: TypeToken<String> = TypeToken.create()
            return PrettyRow(typeToken, cells)
        }

        inline operator fun <reified T: Any> invoke(vararg cells: KeyedCell<T>):PrettyRow<T>{
            val  typeToken: TypeToken<T> = TypeToken.create()
            return PrettyRow(typeToken, cells.toList())
        }
//
//        @PublishedApi
//        internal fun <T : Any> buildRow(
//            token: TypeToken<T>,
//            rowOptions: RowOptions? = null,
//            builder: CellContainer<T>.() -> Unit
//        ): PrettyRow<T> {
//            val constructor = CellContainer<T>(token)
//            builder.invoke(constructor)
//            val realRow = PrettyRow(constructor)
//            if (rowOptions != null) {
//                realRow.options = rowOptions
//            }
//            return realRow
//        }

//        @PublishedApi
//        internal fun <T : Any> buildRowForContext(
//            receiver: T,
//            token: TypeToken<T>,
//            rowOptions: RowOptions? = null,
//            builder: CellReceiverContainer<T>.(T) -> Unit
//        ): PrettyRow<T> {
//            val constructor = CellReceiverContainer<T>(receiver, token)
//            builder.invoke(constructor, receiver)
//            val realRow = PrettyRow(constructor)
//            if (rowOptions != null) {
//                realRow.options = rowOptions
//            }
//            return realRow
//        }
    }
}
