package po.misc.data.pretty_print.parts.rendering

import po.misc.collections.putOverwriting
import po.misc.data.MetaProvider
import po.misc.data.NamedComponent
import po.misc.data.logging.Verbosity
import po.misc.data.output.output
import po.misc.data.pretty_print.cells.AnyRenderingCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.SourceAwareCell
import po.misc.data.pretty_print.cells.StaticRenderingCell
import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.options.Borders
import po.misc.data.pretty_print.parts.options.Margins
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.rows.RowLayout
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.types.token.TypeToken
import po.misc.types.token.safeCast


class RenderAssembler: PrettyHelper{

    internal val stringMap = mutableMapOf<Int, String>()

    fun add(renderRec: RenderRecord, index: Int){
        stringMap.putOverwriting(index,  renderRec.formatted){
            "Text: $it overwritten at index $index".output(Colour.YellowBright)
        }
    }
    fun update(index: Int, renderRec: RenderRecord){
        stringMap[index] = renderRec.formatted
    }
    operator fun set(index: Int, renderRec: RenderRecord){
        add(renderRec, index)
    }
    operator fun get(index: Int): String{
        return stringMap[index] ?: ""
    }
    fun assemble(orientation: Orientation):String{
        val text = stringMap.values.toList().joinRender(orientation)
        stringMap.clear()
        return text
    }
}

class RowRenderPlanner<T>(
    val receiverType: TypeToken<T>,
    var verbosity: Verbosity = Verbosity.Warnings
): MetaProvider, NamedComponent {

    internal var nodeMap = mutableMapOf<Int, CellRenderNode<*>>()
        private set

    val nodes : List<CellRenderNode<*>> get() = nodeMap.values.toList()

    var parameters: RowParameters = RowParameters(RowOptions(Orientation.Vertical))
        private set

    val options: RowOptions get() = parameters.options
    val borders: Borders get() = options.borders

    val margins: Margins = Margins(0)

    val declaredRowWidth: Int get() = parameters.declaredWidth
    val rowWidth: Int get() {
       return nodes.sumOf { it.width }
    }

    val leftMargin: Int get() = margins.leftMargin
    val hasMargins: Boolean get() =  leftMargin != 0
    val hasBorders: Boolean get() = borders.hasBorders
    val size: Int get() = nodes.size

    val rowStats: String get() {
       return buildString {
           appendLine("Orientation: ${parameters.orientation} ")
           appendLine("Declared width: $declaredRowWidth ")
           appendLine("Width: $rowWidth ")
        }
    }

    var gridParameters: GridParameters? = null
        private set

    var isConfigured:Boolean = false

    var renderAssembler  : RenderAssembler = RenderAssembler()

    override val metaText: String get() = parameters.valuesText
    override val name: String get() = "RowRenderPlanner<${receiverType.typeName}>"

    private fun calculateTrim(node: CellRenderNode<*>, modifier: Int): Int{
       val currentLength = node.renderRecord?.plainValueSize?:0
       val trimLength =  currentLength -  node.borders.displaySize
       return trimLength - modifier
    }

    private fun shapeRow(){
        if (nodes.none { !it.reSized }) return
        for (renderedNode in nodes.reversed()) {
            val deficit = rowWidth - declaredRowWidth
            if (deficit <= 0) {
                break
            }
            val maxFree =  renderedNode.width - renderedNode.projectedSize
            if (deficit > maxFree) {
                val trimBy = calculateTrim(renderedNode, maxFree)
                if (verbosity == Verbosity.Debug){
                    deficit.output("deficit")
                    maxFree.output("maxFree")
                    trimBy.output("trimBy(deficit > maxFree)")
                }
                renderedNode.reRender(trimBy)
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

    private fun onNodeMeasure(node: CellRenderNode<*>, record : RenderRecord) {
        if(!node.reSized) {
            if(parameters.layout == RowLayout.Stretch){
                node.setRecord(record, updateWidth = false)
            }else{
                node.setRecord(record, updateWidth = true)
            }
        }
        return
    }
    private fun  onNodeRenderComplete(node: CellRenderNode<*>, record: RenderRecord){
        node.applyBorders(record)
        if(node.reSized){
            if(parameters.layout == RowLayout.Stretch){
                node.setRecord(record, updateWidth = false)
            }else{
                node.setRecord(record, updateWidth = true)
            }
            renderAssembler.update(node.index, record)
        }else{
            renderAssembler[node.index] = record
        }
        if(node.position == NodePosition.LAST || node.position == NodePosition.SINGLE ){
            if(declaredRowWidth < rowWidth && !node.reSized){
                shapeRow()
            }
        }
    }

    private fun tryComputeMargins(){
        if(parameters.layout != RowLayout.Centered) return
        val allWidthResolved = nodes.all { it.width !=  0 }
        if (allWidthResolved) {
            val marginSize = (declaredRowWidth - rowWidth) / 2
            margins.leftMargin = marginSize
        }
    }
    private fun adjustRowValues(keyParams: GridParameters){
        if (parameters.declaredWidth > keyParams.declaredWidth){
            parameters.declaredWidth = keyParams.declaredWidth
        }
    }

    private fun decideInnerBorders(node:  CellRenderNode<*>){
        with(node.borders) {
            when (node.position) {
                NodePosition.MIDDLE -> leftBorder.enabled = true
                NodePosition.LAST -> leftBorder.enabled = true
                NodePosition.FIRST, NodePosition.SINGLE -> disable()
            }
        }
    }

    private fun createCompact(cells: List<PrettyCellBase<*>>,  opts: RowOptions){
        val cellsCount = cells.size
        cells.forEachIndexed { index, cell ->
            val node = when (cell) {
                is StaticRenderingCell -> StaticRenderNode(cell, index,  cellsCount,  opts)
                is SourceAwareCell<*> -> BoundRenderNode(cell,  index,  cellsCount, opts)
                is AnyRenderingCell -> ValueRenderNode(cell, index,cellsCount, opts)
            }
            decideInnerBorders(node)
            node.onRenderComplete(::onNodeRenderComplete)
            node.onMeasure(::onNodeMeasure)
            nodeMap[index] = node
        }
    }
    private fun createStretch(cells: List<PrettyCellBase<*>>, opts: RowOptions){
        val cellsCount = cells.size
        val cellWidth = parameters.declaredWidth / cellsCount
        cells.forEachIndexed { index, cell ->
            val node = when (cell) {
                is StaticRenderingCell -> StaticRenderNode(cell, index,  cellsCount,  opts)
                is SourceAwareCell<*> -> BoundRenderNode(cell,  index,  cellsCount, opts)
                is AnyRenderingCell -> ValueRenderNode(cell, index,cellsCount, opts)
            }
            node.width = cellWidth
            decideInnerBorders(node)
            node.onRenderComplete(::onNodeRenderComplete)
            node.onMeasure(::onNodeMeasure)
            nodeMap[index] = node
        }
        if(parameters.layout == RowLayout.Centered){
            tryComputeMargins()
        }
    }
    private fun createCentered(cells: List<PrettyCellBase<*>>, opts: RowOptions){
        val cellsCount = cells.size
        cells.forEachIndexed { index, cell ->
            val node = when (cell) {
                is StaticRenderingCell -> StaticRenderNode(cell, index,  cellsCount,  opts)
                is SourceAwareCell<*> -> BoundRenderNode(cell,  index,  cellsCount, opts)
                is AnyRenderingCell -> ValueRenderNode(cell, index,cellsCount, opts)
            }
            decideInnerBorders(node)
            node.onRenderComplete(::onNodeRenderComplete)
            node.onMeasure(::onNodeMeasure)
            nodeMap[index] = node
        }
    }

    fun createRenderNodes(cells: List<PrettyCellBase<*>>, opts: RowOptions): List<CellRenderNode<*>> {
        nodeMap.clear()
        val useOptions = gridParameters?.let {
            opts.copy(it)
        }?:opts

        parameters = RowParameters(useOptions)
        if (cells.isNotEmpty()) {
          when (parameters.layout) {
                RowLayout.Compact -> createCompact(cells, useOptions)
                RowLayout.Stretch -> createStretch(cells, useOptions)
                RowLayout.Centered -> createCentered(cells, useOptions)
            }
            isConfigured = true
            return nodeMap.values.toList()
        }else{
            return emptyList()
        }
    }
    fun parametrizeRender(parameters: GridParameters){
        gridParameters = parameters
        adjustRowValues(parameters)
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

    fun assemble(): String {
        nodes.forEach { it.finalizeRender() }
        return renderAssembler.assemble(parameters.orientation)
    }

    operator fun get(index: Int): CellRenderNode<*>? {
        return nodes.getOrNull(index)
    }
    override fun toString(): String = buildString {
        append("RowRenderPlanner ")
        append(parameters.valuesText)
    }
}