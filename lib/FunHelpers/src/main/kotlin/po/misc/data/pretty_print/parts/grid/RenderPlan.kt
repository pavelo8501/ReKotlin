package po.misc.data.pretty_print.parts.grid

import po.misc.collections.putOverwriting
import po.misc.counters.SimpleJournal
import po.misc.data.logging.Verbosity
import po.misc.data.output.output
import po.misc.data.pretty_print.*
import po.misc.data.pretty_print.parts.common.RenderData
import po.misc.data.pretty_print.parts.decorator.Decorator
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.rendering.KeyRenderParameters
import po.misc.data.pretty_print.parts.rendering.RenderParameters
import po.misc.data.pretty_print.templates.TemplateCompanion
import po.misc.data.pretty_print.templates.TemplatePlaceholder
import po.misc.data.styles.Colour
import po.misc.data.text_span.StyledPair
import po.misc.data.text_span.TextSpan
import po.misc.debugging.stack_tracer.TraceOptions
import po.misc.exceptions.error
import po.misc.interfaces.named.NamedComponent
import po.misc.types.safeCast
import po.misc.types.token.TokenizedResolver
import po.misc.types.token.TypeToken
import po.misc.types.token.filterTokenized
import po.misc.types.token.safeCast

/**
 * A compiled render execution plan for a given receiver type [S] and rendered value type [T].
 *
 * A [RenderPlan] is a **supporting structure** owned by a grid or template host.
 * It is responsible for:
 *
 * - Maintaining render order
 * - Collecting local renderable elements
 * - Expanding foreign render plans (e.g. transition grids, placeholders)
 * - Delegating rendering via configured emission functions
 *
 * Root grids themselves are **not** render plan elements — only their
 * renderable parts participate in rendering.
 *
 * @param S the receiver (host) type driving rendering
 * @param T the value type rendered by elements in this plan
 */
class RenderPlan <S, T>(
    val host: TemplateHost<S, T>,
    val keyParams: KeyRenderParameters = KeyRenderParameters(),
    internal val renderBacking: MutableMap<RenderKey, RenderNode<T>> = mutableMapOf(),
): TokenizedResolver<S, T>, NamedComponent, RenderParameters by keyParams {
    private var orderCounter = -1
    override val sourceType: TypeToken<S> get() =  host.sourceType
    override val receiverType:TypeToken<T> get() = host.receiverType

    override val name: String = "RenderPlan"
    override val onBehalfName:TextSpan get() = host.displayName

    internal val decorator: Decorator = Decorator()
    internal val journal = SimpleJournal(this::class)
    override var verbosity: Verbosity = Verbosity.Warnings
        set(value) {
            field = value
            journal.verbosity = value
            decorator.verbosity = value
        }
    val foreignNodes: MutableMap<RenderKey, SelfContainedNode<*>> = mutableMapOf()

    val renderMap : Map<RenderKey, RenderNode<T>> get() =  renderBacking
    val renderSize : Int get() = renderMap.values.sumOf { it.size }
    val size: Int get() = renderMap.size + foreignNodes.size

    val renderNodes: List<RenderNode<*>> get() {
       return buildList {
            addAll(renderBacking.values)
            addAll(foreignNodes.values)
        }
    }
    internal var initialized: Boolean = false
        private set

    init {
        if(foreignNodes.isNotEmpty() || renderMap.isNotEmpty()){
            orderCounter = foreignNodes.size + renderMap.size
        }
    }
    private fun nextOrder(): Int = ++ orderCounter
    internal fun initializeOptions(options: RowOptions) {
        initialized = true
        decorator.addSeparator(options)
        keyParams.initByOptions(options)
    }

    @PublishedApi
    internal fun getNodesOrdered(): List<RenderNode<T>>{
        return renderBacking.values.sortedBy { it.index }
    }
    fun add(placeholder: TemplatePlaceholder<*>):PlaceholderNode<*>{
        val placeholder = PlaceholderNode(this, placeholder,  nextOrder())
        foreignNodes.putOverwriting(placeholder.key, placeholder){
            "Foreign nodes map key ${it.key} was overwritten by ${placeholder.key}".output(Colour.YellowBright)
        }
        return placeholder
    }
    fun add(valueGrid: PrettyValueGrid<T, *>):ValueGridNode<T, *>{
        val valueNode = ValueGridNode(this, valueGrid, nextOrder())

        renderBacking.putOverwriting(valueNode.key, valueNode){
            it.output("${valueNode.key} was overwritten by ${valueNode.key}")
        }
        return valueNode
    }
    fun add(valueRow: PrettyValueRow<T, *>): ValueRowNode<T>{
        val rowNode = ValueRowNode(this, valueRow, nextOrder())
        renderBacking.putOverwriting(rowNode.key, rowNode){
            it.output("${rowNode.key} was overwritten by ${rowNode.key}")
        }
        return rowNode
    }
    fun add(row: PrettyRow<T>): RowNode<T>{
        val rowNode = RowNode(this, row, nextOrder())
        renderBacking.putOverwriting(rowNode.key, rowNode){
            it.output("${rowNode.key} was overwritten by ${rowNode.key}")
        }
        return rowNode
    }
    fun add(key: RenderKey, renderNode:  RenderNode<T>):RenderNodeBase<T>{
        renderBacking.putOverwriting(key, renderNode){
            it.output("$key was overwritten by $renderNode")
        }
        return renderNode as RenderNodeBase<T>
    }

    fun getJoinedRenderables(): List<RenderNode<*>>{
        val ownRenderables = renderMap.values
        val foreignRenderables = foreignNodes.flatMap { getJoinedRenderables() }
        val joinedRenderables = buildList {
            addAll(ownRenderables)
            addAll(foreignRenderables)
        }
        return joinedRenderables.sortedBy { it.key.index }
    }
    inline fun <reified E: TemplatePart<T2>, reified T2> getRenderable(key: RenderKey? = null): E? {
       return if(key != null){
           get(key).safeCast<E, T2>()
        }else{
            renderNodes.map { it.element }.filterTokenized<E, T2>().firstOrNull()
        }
    }

    fun render(receiver:T):RenderData{
        val renderResult = mutableListOf<RenderData>()
        val nodes = getNodesOrdered()
        val nodesCount = nodes.size
        for(i in 0 until nodesCount){
            val node = nodes[i]
            val nodeData =  node.scopedRender(receiver)
            keyParams.updateWidth(node.sourceWidth)
            renderResult.add(nodeData)
        }
        val decoration =  decorator.decorate(renderResult, keyParams)
        val data = RenderData(keyParams.createSnapshot(name), decoration)
        return data
    }
    /**
     * @param parameters Upper RenderPlan
     * @param list list of resolved from source receivers
     */
    fun scopedRender(parameters:  RenderParameters, list: List<T>):RenderData{
        val renderResult = mutableListOf<RenderData>()
        keyParams.updateWidth(parameters.contentWidth)
        for(t in 0 until list.size){
            val receiver =  list[t]
            val nodes = getNodesOrdered()
            val nodesCount = nodes.size
            for(i in 0 until nodesCount){
                val node = nodes[i]
                val nodeData =  node.scopedRender(receiver)
                renderResult.add(nodeData)
            }
        }
        return if(renderResult.isNotEmpty()){
            val decoration =  decorator.decorate(renderResult, keyParams)
            RenderData(keyParams.createSnapshot(name), decoration)

        }else{
            "Empty content about to be rendered. Is this planned?".output(Colour.YellowBright)
            val empty = StyledPair()
            val decoration = decorator.decorate(empty, this)
            RenderData(keyParams.createSnapshot(name), decoration)
        }
     }
    fun info():RenderPlanSnapshot {
        val snp = RenderPlanSnapshot(this)
        return snp
    }
    fun copy(): RenderPlan<S, T> {
        val renderPlanCopy = RenderPlan(host)
        val copiedEnderMap = renderMap.values.map { it.copy() }
        copiedEnderMap.forEach {
            renderPlanCopy.renderBacking[it.key] = it
        }
        val copiedForeignMap = foreignNodes.values.map { it.copy() }
        copiedForeignMap.forEach {
            renderPlanCopy.foreignNodes[it.key] = it as  SelfContainedNode<*>
        }
        renderPlanCopy.orderCounter = orderCounter
        return renderPlanCopy
    }
    fun clear(){
        renderBacking.clear()
        foreignNodes.values.forEach {it.clear()}
        foreignNodes.clear()
    }
    override fun toString(): String = displayName.plain

    operator fun get(type: RenderableType): List<TemplatePart<*>> {
        val filtered = renderNodes.filter {
            it.key.renderableType == type
        }.map { it.element }
        return filtered
    }
    operator fun <T2: TemplatePart<*>>  get(type: TemplateCompanion<T2>): List<T2>  {
        val elements = renderNodes.map { it.element } + host as TemplatePart<*>
        val filtered =  elements.filter { it::class == type.templateClass }
        return  filtered.mapNotNull { it.safeCast(type.templateClass) }
    }
    operator fun get(renderKey: RenderKey):  TemplatePart<*> {
        return renderMap[renderKey]?.element?:run {
            foreignNodes[renderKey]?.element?:run {
                error("Renderable with key $renderKey not found.", info(), TraceOptions.ThisMethod)
            }
        }
    }
}
