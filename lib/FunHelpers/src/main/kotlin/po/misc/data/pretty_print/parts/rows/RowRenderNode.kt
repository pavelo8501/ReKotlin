package po.misc.data.pretty_print.parts.rows

import po.misc.data.MetaProvider
import po.misc.data.pretty_print.cells.AnyRenderingCell
import po.misc.data.pretty_print.cells.RenderableCell
import po.misc.data.pretty_print.cells.SourceAwareCell
import po.misc.data.pretty_print.cells.StaticRenderingCell
import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.options.InnerBorders
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.render.CellParameters
import po.misc.data.pretty_print.parts.render.LayerType
import po.misc.data.pretty_print.parts.render.RenderCanvas
import po.misc.data.text_span.MutablePair
import po.misc.data.text_span.TextSpan
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
    private val cell: RenderableCell,
):MetaProvider, CellParameters{

    val innerBorders : InnerBorders = InnerBorders()
    val options: Options get() = cell.renderOptions

    override val layout: Layout get() = host.keyParams.layout
    override var maxWidth: Int = options.width
    override val availableWidth: Int get() = maxWidth - innerBorders.displaySize
    override val leftOffset: Int = 0

    val keySegmentSize: Int get() {
        return when(val record = renderRecord){
            is RenderRecord -> {
                (record.key?.plainLength?:0) + record.separator.displaySize
            }
            else -> record.plainLength
        }
    }

    override val metaText: String get() {
        return buildString {
            appendLine(name)
            appendLine("Max width: $maxWidth ")
            appendLine("ContentWidth: $contentWidth ")
        }
    }
    var renderRecord:TextSpan = RenderRecord(MutablePair(), null, null)

    var canvas: RenderCanvas = RenderCanvas(LayerType.Dynamic)

    var currentReceiver:T? = null
        private set

    override val contentWidth: Int get() = renderRecord.plainLength

    val name: String = "$nodeName[$cell] #$index"
    val position : NodePosition get() {
        return when {
            totalCells == 1 -> NodePosition.SINGLE
            index == 0 -> NodePosition.FIRST
            index == totalCells - 1 -> NodePosition.LAST
            else -> NodePosition.MIDDLE
        }
    }

    abstract fun render(receiver: T): TextSpan

    protected fun signalComplete(record: TextSpan, receiver:T): TextSpan{
        canvas.clear()

        canvas.addSpan(record)
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

    override fun render(receiver: Unit): TextSpan{
        return with(cell){
            val record = renderInScope()
            signalComplete(record, receiver)
        }
    }
    fun render(opts: Options):TextSpan{
        cell.renderOptions = opts
        return render(Unit)
    }
}

class ValueRenderNode(
    host: RowRenderPlanner<*>,
    index: Int,
    totalCells: Int,
    val cell: AnyRenderingCell,
): RowRenderNode<Any>("ValueRenderNode", host, index, totalCells, cell){

    override fun render(receiver: Any): TextSpan{
        return with(cell){
            val record = renderInScope(receiver)
            signalComplete(record, receiver)

        }
    }
    fun render(receiver: Any, opts: Options):TextSpan {
        cell.renderOptions = opts
        return render(receiver)
    }
}

class BoundRenderNode<T>(
    host: RowRenderPlanner<*>,
    index: Int,
    totalCells: Int,
    val cell: SourceAwareCell<T>,
): RowRenderNode<T>("BoundRenderNode", host,  index, totalCells, cell), Tokenized<T>{

    override val typeToken: TypeToken<T> = cell.sourceType

    override fun render(receiver: T): TextSpan{
        return with(cell){
            val record = renderInScope(receiver)
            signalComplete(record, receiver)
        }
    }
    fun render(receiver: T,  opts: Options):TextSpan{
        cell.renderOptions = opts
        return render(receiver)
    }
}