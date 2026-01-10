package po.misc.data.pretty_print.parts.rendering

import po.misc.data.MetaProvider
import po.misc.data.pretty_print.cells.AnyRenderingCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.RenderableCell
import po.misc.data.pretty_print.cells.SourceAwareCell
import po.misc.data.pretty_print.cells.StaticRenderingCell
import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.options.InnerBorders
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.rows.RowLayout
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken

enum class NodePosition { FIRST, MIDDLE, LAST, SINGLE }


sealed class CellRenderNode<T>(
    nodeName: String,
    private val cell: RenderableCell<T>,
    override val index: Int,
    val totalCells: Int,
    internal val rowOptions: RowOptions,
):MetaProvider, CellRenderParameters,  Tokenized<T>{

    final override val typeToken: TypeToken<T> = cell.sourceType
    override var width: Int = 0
        internal set

    override var trimTo: Int? = null
    override val orientation: Orientation get() = rowOptions.orientation
    override val layout: RowLayout get() = rowOptions.layout
    override val declaredWidth: Int get() = cell.currentRenderOpts.width
    override val keySegmentSize: Int get() = cell.keySegmentSize
    override val projectedSize: Int get() {
       return  cell.keySegmentSize + borders.displaySize
    }
    override val metaText: String get() {
        return buildString {
            appendLine(name)
            appendLine("Declared width: $declaredWidth ")
            appendLine("Width: $width ")
        }
    }

    protected var lastUsedReceiver:T? = null
        private set
    internal var onRenderComplete: ((CellRenderNode<T>, RenderRecord) -> Unit)? = null
    internal var onMeasure : ((CellRenderNode<*>, RenderRecord) -> Unit)? = null

    val borders : InnerBorders = InnerBorders()
    var renderRecord:RenderRecord? = null
        private set

    val reSized : Boolean get() = trimTo != null
    val name: String = "$nodeName<$typeName> #$index"
    val position : NodePosition get() {
        return when {
            totalCells == 1 -> NodePosition.SINGLE
            index == 0 -> NodePosition.FIRST
            index == totalCells - 1 -> NodePosition.LAST
            else -> NodePosition.MIDDLE
        }
    }

    init {
        cell.parametrizeRender(this)
        borders.leftBorder.applyValues(rowOptions.cellSeparator)
    }

    internal fun onRenderComplete(callback: (CellRenderNode<T>, RenderRecord) -> Unit){
        onRenderComplete = callback
    }
    internal fun onMeasure(callback : (CellRenderNode<*>, RenderRecord) -> Unit){
        onMeasure = callback
    }
    internal fun setRecord(record:RenderRecord, updateWidth: Boolean){
        renderRecord = record
        if(updateWidth){
            width = record.totalPlainLength
        }
    }

    protected fun preSaveReceiver(receiver:T):T{
        lastUsedReceiver = receiver
        return receiver
    }
    override fun renderComplete(record : RenderRecord){
        onRenderComplete?.invoke(this, record)
    }
    override fun measureWidth(record: RenderRecord){
        if(!reSized) {
            onMeasure?.invoke(this, record)
        }
    }

    abstract fun render(receiver: T): String

    fun reRender(trimToSize: Int){
        trimTo = trimToSize
        lastUsedReceiver?.let {
            render(it)
        }
    }
    fun applyBorders(editablePair: RenderRecord){
        borders.wrapText(editablePair)
    }
    fun finalizeRender(){
        lastUsedReceiver = null
        renderRecord = null
        onMeasure = null
        onRenderComplete = null
    }
    override fun toString(): String  = name
}

class StaticRenderNode(
    val cell: StaticRenderingCell,
    index: Int,
    totalCells: Int,
    rowOptions: RowOptions,
): CellRenderNode<Unit>("StaticRenderNode", cell, index, totalCells, rowOptions){

    override fun render(receiver: Unit):String{
        preSaveReceiver(receiver)
        return cell.render()
    }
    fun render():String = render(Unit)

    fun render(opts: Options):String{
        preSaveReceiver(Unit)
        return cell.render(opts)
    }
}

class ValueRenderNode(
    val cell: AnyRenderingCell,
    index: Int,
    totalCells: Int,
    rowOptions: RowOptions,
): CellRenderNode<Any>("ValueRenderNode", cell,index, totalCells, rowOptions){

    override fun render(receiver: Any):String {
        return cell.render(preSaveReceiver(receiver))
    }
    fun render(receiver: Any, opts: Options):String {
        return cell.render(preSaveReceiver(receiver), opts)
    }
}

class BoundRenderNode<T>(
    val cell: SourceAwareCell<T>,
    index: Int,
    totalCells: Int,
    rowOptions: RowOptions,
): CellRenderNode<T>("BoundRenderNode", cell, index,  totalCells,  rowOptions){

    override fun render(receiver: T):String {
        return cell.render(preSaveReceiver(receiver))
    }
    fun render(receiver: T,  opts: Options):String{
        return cell.render(preSaveReceiver(receiver), opts)
    }
}