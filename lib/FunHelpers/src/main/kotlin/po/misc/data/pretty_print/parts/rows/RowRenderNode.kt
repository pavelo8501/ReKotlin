package po.misc.data.pretty_print.parts.rows

import po.misc.data.MetaProvider
import po.misc.data.pretty_print.cells.AnyRenderingCell
import po.misc.data.pretty_print.cells.RenderableCell
import po.misc.data.pretty_print.cells.SourceAwareCell
import po.misc.data.pretty_print.cells.StaticRenderingCell
import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.options.InnerBorders
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.rendering.CellParameters
import po.misc.data.text_span.MutablePair
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken

enum class NodePosition { FIRST, MIDDLE, LAST, SINGLE }

/**
 * @property maxWidth Maximal width cell should occupy excluding decorators(borders)
 * @property availableWidth maxWidth - borders size
 */
sealed class RowRenderNode<T>(
    nodeName: String,
    protected val host: RowRenderPlanner<*>,
    override val index: Int,
    val totalCells: Int,
    private val cell: RenderableCell<T>,
):MetaProvider, CellParameters, Tokenized<T>{

    final override val typeToken: TypeToken<T> = cell.sourceType
    val innerBorders : InnerBorders = InnerBorders()
    val options : Options get() = cell.currentRenderOpts

    override val layout: Layout get() = host.keyParams.layout
    override var maxWidth: Int = options.width
    override val availableWidth: Int get() = maxWidth - innerBorders.displaySize
    override val leftOffset: Int = 0

    val keySegmentSize: Int get() {
        return (renderRecord.key?.plainLength?:0) + renderRecord.separator.displaySize
    }

    override val metaText: String get() {
        return buildString {
            appendLine(name)
            appendLine("Max width: $maxWidth ")
            appendLine("ContentWidth: $contentWidth ")
        }
    }
    var renderRecord:RenderRecord = RenderRecord(MutablePair(), null, null)

    var currentReceiver:T? = null
        private set

    override val contentWidth: Int get() = renderRecord.plainLength

    val name: String = "$nodeName<$typeName> #$index"
    val position : NodePosition get() {
        return when {
            totalCells == 1 -> NodePosition.SINGLE
            index == 0 -> NodePosition.FIRST
            index == totalCells - 1 -> NodePosition.LAST
            else -> NodePosition.MIDDLE
        }
    }

    abstract fun render(receiver: T): RenderRecord

    protected fun signalComplete(record : RenderRecord, receiver:T):RenderRecord{
        renderRecord = record
        currentReceiver = receiver
        return record
    }

    fun reRender(newWidth: Int){
        maxWidth = newWidth
        val receiver = currentReceiver
        if(receiver != null) {
            render(receiver)
        }
    }
    fun applyBorders(){
        innerBorders.wrapText(renderRecord)
    }
    fun finalizeRender(){
        currentReceiver = null
    }
    override fun toString(): String  = name
}

class StaticRenderNode(
    host: RowRenderPlanner<*>,
    index: Int,
    totalCells: Int,
    val cell: StaticRenderingCell,
): RowRenderNode<Unit>("StaticRenderNode", host, index, totalCells, cell){

    override fun render(receiver: Unit): RenderRecord{
        return with(cell){
            val record = scopedRender(receiver)
            signalComplete(record, receiver)
        }
    }
    fun render(receiver: Unit,  opts: Options):RenderRecord{
        cell.currentRenderOpts = opts
        return render(Unit)
    }
}

class ValueRenderNode(
    host: RowRenderPlanner<*>,
    index: Int,
    totalCells: Int,
    val cell: AnyRenderingCell,
): RowRenderNode<Any>("ValueRenderNode", host, index, totalCells, cell){

    override fun render(receiver: Any): RenderRecord{
        return with(cell){
            val record = scopedRender(receiver)
            signalComplete(record, receiver)
        }
    }
    fun render(receiver: Any, opts: Options):RenderRecord {
        cell.currentRenderOpts = opts
        return render(receiver)
    }
}

class BoundRenderNode<T>(
    host: RowRenderPlanner<*>,
    index: Int,
    totalCells: Int,
    val cell: SourceAwareCell<T>,
): RowRenderNode<T>("BoundRenderNode", host,  index, totalCells, cell){

    override fun render(receiver: T): RenderRecord{
        return with(cell){
            val record = scopedRender(receiver)
            signalComplete(record, receiver)
        }
    }
    fun render(receiver: T,  opts: Options):RenderRecord{
        cell.currentRenderOpts = opts
        return render(receiver)
    }
}