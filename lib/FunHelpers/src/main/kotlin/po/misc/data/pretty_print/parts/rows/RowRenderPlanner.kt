package po.misc.data.pretty_print.parts.rows

import po.misc.data.MetaProvider
import po.misc.data.logging.Verbosity
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyRowBase
import po.misc.data.pretty_print.cells.AnyRenderingCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.SourceAwareCell
import po.misc.data.pretty_print.cells.StaticRenderingCell
import po.misc.data.pretty_print.parts.decorator.Decorator
import po.misc.data.pretty_print.parts.common.RenderData
import po.misc.data.pretty_print.parts.options.Margins
import po.misc.data.pretty_print.parts.rendering.KeyRenderParameters
import po.misc.data.pretty_print.parts.rendering.RenderParameters
import po.misc.data.text_span.joinSpans
import po.misc.interfaces.named.NamedComponent
import po.misc.types.token.TypeToken
import po.misc.types.token.safeCast

class RowRenderPlanner<T>(
    val host: PrettyRowBase<*, T>,
    val keyParams :KeyRenderParameters = KeyRenderParameters()
): MetaProvider, NamedComponent, RenderParameters by keyParams{

    val receiverType: TypeToken<T> get() = host.receiverType
    override var verbosity: Verbosity = Verbosity.Warnings

    private val nodesBacking: MutableList<RowRenderNode<*>> = mutableListOf()
    private val nodesTotalWidth:Int get() {
      //  val spaceOccupiedByBorders = nodes.sumOf { it.innerBorders.displaySize }
        val nodesContentWidth = nodes.sumOf { it.contentWidth }
        return nodesContentWidth
    }

    val nodes: List<RowRenderNode<*>> get() = nodesBacking
    val decorator: Decorator = Decorator()
    val margins: Margins = Margins(0)

    override val metaText: String get() {
       return buildString {
           appendLine("Orientation: $orientation ")
           appendLine("Max width: $maxWidth ")
           appendLine("Content width: $contentWidth ")
        }
    }

    var isConfigured:Boolean = false
    override val name: String get() = "RowRenderPlanner<${receiverType.typeName}>"

    init {
        keyParams.onUpdated {
            decorator.addSeparator(host.options)
        }
    }

    private fun prepareNewRender(){
        nodesBacking.clear()
    }
    private fun calculateTrim(node: RowRenderNode<*>, deficit: Int): Int{
        return  node.contentWidth -  node.innerBorders.displaySize  - deficit
    }
    private fun shapeRow(){
        for (renderedNode in nodes.reversed()) {
            val deficit = contentWidth - maxWidth
            if (deficit <= 0) {
                break
            }
            val maxFree =  renderedNode.contentWidth - renderedNode.keySegmentSize
            if (deficit > maxFree){
                if (verbosity == Verbosity.Debug){
                    deficit.output("deficit")
                    maxFree.output("maxFree")
                }
                renderedNode.reRender(maxFree)
            }else{
                val trimBy =  calculateTrim(renderedNode, deficit)
                if (verbosity == Verbosity.Debug) {
                    deficit.output("deficit")
                    maxFree.output("maxFree")
                    trimBy.output("trimBy(deficit <= maxFree)")
                }
                renderedNode.reRender(trimBy)
                return
            }
        }
    }

    private fun applyInnerBorders(){
        nodes.forEach { node ->
            node.applyBorders()
        }
    }
    private fun tryComputeMargins(){
        if(layout != Layout.Centered) return
        val allWidthResolved = nodes.all { it.contentWidth !=  0 }
        if (allWidthResolved) {
            val marginSize = (maxWidth - contentWidth) / 2
            margins.leftMargin = marginSize
        }
    }
    private fun decideInnerBorders(node: RowRenderNode<*>){
        with(node.innerBorders) {
            when (node.position) {
                NodePosition.MIDDLE -> leftBorder.enabled = true
                NodePosition.LAST -> leftBorder.enabled = true
                NodePosition.FIRST, NodePosition.SINGLE -> disable()
            }
        }
    }

    private fun createCompact(cells: List<PrettyCellBase<*>>){
        val count = cells.size
        cells.forEachIndexed { index, cell ->
            val node = when (cell) {
                is StaticRenderingCell -> StaticRenderNode(this, index, count, cell)
                is SourceAwareCell<*> -> BoundRenderNode(this, index, count, cell)
                is AnyRenderingCell -> ValueRenderNode(this, index, count, cell)
            }
            decideInnerBorders(node)
            nodesBacking.add(node)

        }
    }
    private fun createStretch(cells: List<PrettyCellBase<*>>){
        val count = cells.size
        cells.forEachIndexed { index, cell ->
            val node = when (cell) {
                is StaticRenderingCell -> StaticRenderNode(this, index, count, cell)
                is SourceAwareCell<*> -> BoundRenderNode(this, index, count, cell)
                is AnyRenderingCell -> ValueRenderNode(this, index, count, cell)
            }
            decideInnerBorders(node)
            nodesBacking.add(node)
        }
        val cellWidth = maxWidth / count
        nodes.forEach { it.maxWidth = cellWidth }
        if(layout == Layout.Centered){
            tryComputeMargins()
        }
    }
    private fun createCentered(cells: List<PrettyCellBase<*>>){
        val count = cells.size
        cells.forEachIndexed { index, cell ->
            val node = when (cell) {
                is StaticRenderingCell -> StaticRenderNode(this, index, count, cell)
                is SourceAwareCell<*> -> BoundRenderNode(this, index, count, cell)
                is AnyRenderingCell -> ValueRenderNode(this, index, count, cell)
            }
            decideInnerBorders(node)
            nodesBacking.add(node)
        }
    }

    fun createRenderNodes(cells: List<PrettyCellBase<*>>, hostParameters: RenderParameters? = null): List<RowRenderNode<*>> {
        if(hostParameters != null){
            keyParams.implyConstraints(hostParameters)
        }
        prepareNewRender()
        if (cells.isNotEmpty()) {
          when (keyParams.layout) {
                Layout.Compact -> createCompact(cells)
                Layout.Stretch -> createStretch(cells)
                Layout.Centered -> createCentered(cells)
            }
            isConfigured = true
            return nodes
        }else{
            return emptyList()
        }
    }

    fun checkSourceAware(instance: Any, parameter: BoundRenderNode<*>): Pair<T, BoundRenderNode<T>>? {
        val castedInstance = instance.safeCast(receiverType) ?: return null
        val castedParam = parameter.safeCast<BoundRenderNode<T>> {
            castedInstance
        }
        if (castedParam == null) return null
        return Pair(castedInstance, castedParam)
    }
    fun getStatic(): List<StaticRenderNode>{
        return nodes.filterIsInstance<StaticRenderNode>()
    }

    fun finalizeRender():RenderData{
        applyInnerBorders()
        keyParams.updateWidth(nodesTotalWidth)
        shapeRow()
        val renderRecords = nodes.map { it.renderRecord }
        val decoration = decorator.decorate(renderRecords.joinSpans(orientation), keyParams.createSnapshot(host))
        keyParams.updateWidth(decoration.contentWidth)
        nodes.forEach { it.finalizeRender() }
        return RenderData(keyParams.createSnapshot(host), decoration)
    }
    fun clear(){
        nodes.forEach { it.finalizeRender() }
        nodesBacking.clear()
    }
    operator fun get(index: Int): RowRenderNode<*>? {
        return nodes.getOrNull(index)
    }
    override fun toString(): String = buildString {
        append("RowRenderPlanner ")
        append(metaText)
    }

}