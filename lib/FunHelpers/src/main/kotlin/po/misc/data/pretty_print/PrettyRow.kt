package po.misc.data.pretty_print

import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
import po.misc.callbacks.validator.ValidityCondition
import po.misc.context.tracable.TraceableContext
import po.misc.data.helpers.orDefault
import po.misc.data.pretty_print.cells.AnyRenderingCell
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.ReceiverAwareCell
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.cells.StaticRender
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.options.CellOptions
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.RowPresets
import po.misc.data.pretty_print.parts.rows.RowParams
import po.misc.data.pretty_print.parts.template.NamedTemplate
import po.misc.data.pretty_print.parts.template.RowID
import po.misc.data.pretty_print.parts.template.TemplateData
import po.misc.data.pretty_print.rows.RowBuilderBase
import po.misc.data.pretty_print.templates.TemplateCompanion
import po.misc.data.strings.joinToStringNotBlank
import po.misc.data.strings.stringify
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.types.castBaseOrThrow
import po.misc.types.castOrThrow
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import po.misc.types.token.safeCast
import kotlin.String

internal fun MutableList<String>.join(orientation: Orientation):String {
   return if(orientation == Orientation.Horizontal){
        joinToString(separator = SpecialChars.EMPTY)
    }else{
        joinToString(separator = SpecialChars.NEW_LINE)
    }
}


class PrettyRow<T>(
    initialCells: List<PrettyCellBase>,
    override val receiverType: TypeToken<T>,
    override var options: RowOptions = RowOptions(Orientation.Horizontal),
    id: RowID? = null,
): RenderableElement<T, T>, TraceableContext, PrettyHelper {

    constructor(container: RowBuilderBase<T, T>):this(container.cells, container.valueType)
    override val valueType: TypeToken<T> get() = receiverType
    private val cellsBacking: MutableList<PrettyCellBase> = mutableListOf()
    val templateData: TemplateData = createTemplateData(id, typeToken)
    override val renderableType: RenderableType = templateData.renderableType
    override val id: NamedTemplate = templateData.templateID

    internal val renderConditions = mutableListOf<ValidityCondition<*>>()

    override var enabled: Boolean = true
    internal set

    val prettyCells: List<PrettyCell> get() = cellsBacking.filterIsInstance<PrettyCell>()
    val staticCells: List<StaticCell> get() = cellsBacking.filterIsInstance<StaticCell>()
    val keyedCells: List<KeyedCell<*>> get() = cellsBacking.filterIsInstance<KeyedCell<*>>()
    val computedCells: List<ComputedCell<*, *>> get() = cellsBacking.filterIsInstance<ComputedCell<*, *>>()

    private val anyRenderingCells : List<AnyRenderingCell> get() {
       return cells.filterIsInstance<AnyRenderingCell>()
    }

    val cells : List<PrettyCellBase> get() = cellsBacking

    val size: Int get() = cells.size
    val rowMaxWidth: Int get()  = cells.maxOf { it.cellOptions.width }

    val beforeRowRender: Signal<RowParams<T>, Unit> = signalOf<RowParams<T>, Unit>()
    val afterRowRender: Signal<RowParams<T>, Unit> = signalOf<RowParams<T>, Unit>()

    var dataLoader: DataLoader<T, T> = DataLoader("PrettyRow", typeToken, typeToken)
        internal set

    internal var currentRenderOpt: RowOptions = options

    private val currentRender = mutableListOf<String>()

    init { setCells(initialCells) }

    private fun renderSelection(receiver:T, cell: PrettyCellBase): String {
        return when (cell) {
            is ReceiverAwareCell<*> -> {
                val casted = cell.safeCast<ReceiverAwareCell<T>>()
                casted?.render(receiver) ?: ""
            }
            is StaticCell -> cell.render()
            else -> cell.render(receiver.stringify().formatted)
        }
    }

    private fun applyRenderOptions(opts: CommonRowOptions? = null){
        PrettyHelper.toRowOptionsOrNull(opts)?.let {
            options.orientation = it.orientation
            options.plainKey = it.plainKey
            options.plainText = it.plainText
        }
    }
    private fun addRender(value: String){
        currentRender.add(value)
    }
    private fun assembleRender():String{
        val render = if(currentRenderOpt.orientation == Orientation.Vertical){
            currentRender.joinToStringNotBlank(SpecialChars.NEW_LINE)
        }else{
            currentRender.joinToStringNotBlank(SpecialChars.EMPTY)
        }
        currentRender.clear()
        return render
    }

    fun render(receiver: T, opts: CommonRowOptions? = null): String {
        currentRenderOpt = toRowOptions(opts, options)
        beforeRowRender.trigger(RowParams(this, currentRenderOpt))
        cells.forEachIndexed { index, cell ->
            val cellRenderedText = renderSelection(receiver, cell)
            addRender(cellRenderedText)
        }
        val render = assembleRender()
        afterRowRender.trigger(RowParams(this, currentRenderOpt, render))
        return render
    }
    fun render(receiver:T,  optionsBuilder: (RowOptions.()-> Unit)): String {
        optionsBuilder.invoke(options)
        return render(receiver, options)
    }

    override fun render(receiverList: List<T>, opts: CommonRowOptions?): String {
        val resultList =  receiverList.map { render(it, opts) }
        return resultList.joinToString(separator = SpecialChars.NEW_LINE)
    }
    fun render(receiverList: List<T>, optionsBuilder: (RowOptions.()-> Unit)): String {
        optionsBuilder.invoke(options)
        return render(receiverList, options)
    }

    override fun resolve(receiverList: List<T>): List<T>{
        return receiverList
    }

    fun renderAnyList(values: List<Any>, opts: CommonRowOptions? = null): String {
        currentRenderOpt = toRowOptions(opts, options)
        val anyRendering = anyRenderingCells
        values.forEachIndexed { index, any ->
            if(receiverType ==  any::class){
                any.safeCast<T>(receiverType)?.let {
                   val cellRender =  render(it, currentRenderOpt)
                   addRender(cellRender)
                }
            }else{
                anyRendering.getOrNull(index)?.let {cell->
                    val cellRender = cell.render(any)
                    addRender(cellRender)
                }?:run {
                    addRender(any.toString())
                }
            }
        }
        return  assembleRender()
    }
    fun renderAny(vararg values: Any, rowOptions: CommonRowOptions? = null): String{
        return renderAnyList(values.toList(), rowOptions)
    }
    fun render(opts: CommonRowOptions? = null):String {
        applyRenderOptions(opts)
        val resolved = dataLoader.resolveValue()
        if (resolved != null) {
            return render(resolved, opts)
        } else {
            val staticRenderers = cells.filterIsInstance<StaticRender>()
            val separator = if (options.orientation == Orientation.Horizontal) {
                SpecialChars.EMPTY
            } else {
                SpecialChars.NEW_LINE
            }
            return staticRenderers.joinToString(separator = separator) { it.render() }
        }
    }

    fun applyOptions(opt: CommonRowOptions?): PrettyRow<T>{
        if(opt != null){
            options = toRowOptions(opt)
        }
        return this
    }
    val cellBuffer: MutableList<PrettyCellBase> = mutableListOf()
    fun bufferCell(newCell: PrettyCellBase){
        cellBuffer.add(newCell)
    }

    fun flushBuffered(){
        if(cellBuffer.isNotEmpty()){
            setCells(cellBuffer)
        }
        cellBuffer.clear()
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

    override fun copy(usingOptions: CommonRowOptions?): PrettyRow<T>{
        val copiedOptions = toRowOptions(usingOptions, options.copy())
        val copiedCells = cells.map { it.copy() }
        val rowCopy = if(id is RowID){
            PrettyRow(copiedCells, valueType, copiedOptions, id)
        }else{
            PrettyRow(copiedCells, valueType, copiedOptions)
        }

        return rowCopy
    }
    override fun equals(other: Any?): Boolean {
        if(other !is PrettyRow<*>) return false
        if(other.typeToken != typeToken) return false
        if(other.cells.size != cells.size) return false
        if(other.id != id) return false
        if(other.dataLoader != dataLoader) return false
        return true
    }
    override fun hashCode(): Int {
        var result =  (id.hashCode() ?: 0)
        result = 31 * result + typeToken.hashCode()
        result = 31 * result + cells.size
        result = 31 * result + dataLoader.hashCode()
        return result
    }
    override fun toString(): String {
        val static = "Static: ${staticCells.size}"
        val keyed = "Keyed: ${keyedCells.size}"
        val computed = "Computed: ${computedCells.size}"
        val pretty = "Pretty: ${prettyCells.size}"
        return "PrettyRow<${typeToken.typeName}>[Id: ${id.orDefault("N/A")} Cells: $static, $keyed, $computed, $pretty]"
    }

    companion object: TemplateCompanion, PrettyHelper {
        val sourceColour : Colour = Colour.Magenta
        val prettyName : String= "Row".colorize(sourceColour)

        @PublishedApi
        internal inline fun <reified T> createEmpty(
            opt: RowOptions? = null
        ):PrettyRow<T>{
            return PrettyRow(emptyList<PrettyCellBase>(),  TypeToken<T>(), PrettyHelper.toRowOptions(opt))
        }

        internal fun <T> createEmpty(
            typeToken: TypeToken<T>,
            opt: RowOptions? = null,
            rowID: RowID? = null,
        ):PrettyRow<T>{
            return PrettyRow(emptyList(), typeToken, PrettyHelper.toRowOptions(opt), rowID)
        }

        @JvmName("invokeStatic")
        operator fun invoke(
            cells: List<StaticCell>,
            options: RowOptions? = null,
            rowID: RowID? = null
        ):PrettyRow<String> = PrettyRow(cells, TypeToken<String>(),toRowOptions(options), rowID)

        inline operator fun <reified T> invoke(
            cells: List<PrettyCellBase> = emptyList(),
            opt: RowOptions? = null,
            rowID: RowID? = null
        ):PrettyRow<T> = PrettyRow(cells,  TypeToken<T>(), toRowOptions(opt), rowID)


        operator fun <T> invoke(
            token:TypeToken<T>,
            cells: List<PrettyCellBase> = emptyList(),
            opt: RowOptions? = null,
            rowID: RowID? = null
        ):PrettyRow<T> = PrettyRow(cells, token, toRowOptions(opt), rowID)


        inline operator fun <reified T: Any> invoke(
            vararg cells: KeyedCell<T>,
            options: RowOptions? = null,
            rowID: RowID? = null
        ):PrettyRow<T> = PrettyRow(cells.toList(), TypeToken<T>(), toRowOptions(options), rowID)

        inline operator fun <reified T> invoke(
            vararg cells: ReceiverAwareCell<T>,
            opt: RowOptions? = null,
            rowID: RowID? = null
        ):PrettyRow<T>{
           val list = cells.toList()
           val row = PrettyRow(emptyList(), TypeToken<T>(), PrettyHelper.toRowOptions(opt), rowID)
           list.forEach {cell->
               when(cell){
                   is ComputedCell<*, *> -> row.bufferCell(cell)
                   is KeyedCell<*> -> row.bufferCell(cell)
               }
           }
           row.flushBuffered()
           return row
        }

        operator fun invoke(
            firstCell: PrettyCellBase,
            vararg cells: AnyRenderingCell,
            opt: RowOptions? = null,
            rowID: RowID? = null
        ):PrettyRow<String>{
            val list = buildList {
                add(firstCell)
                addAll(cells.toList())
           }
           val row = PrettyRow(emptyList(), TypeToken<String>(),  PrettyHelper.toRowOptions(opt), rowID)
           list.forEach {cell->
                when(cell){
                    is ComputedCell<*, *> -> row.bufferCell(cell)
                    is KeyedCell<*> -> row.bufferCell(cell)
                    is StaticCell -> row.bufferCell(cell)
                }
            }
           row.flushBuffered()
           return row
        }
    }
}