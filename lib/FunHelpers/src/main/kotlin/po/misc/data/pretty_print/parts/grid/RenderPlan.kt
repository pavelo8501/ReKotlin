package po.misc.data.pretty_print.parts.grid

import po.misc.collections.putOverwriting
import po.misc.context.component.Component
import po.misc.counters.DataRecord
import po.misc.counters.SimpleJournal
import po.misc.data.ifUndefined
import po.misc.data.logging.Verbosity
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.PrettyValueRow
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.TemplateHost
import po.misc.data.pretty_print.TemplatePart
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.templates.TemplateCompanion
import po.misc.data.pretty_print.templates.TemplatePlaceholder
import po.misc.data.strings.joinToStringNotBlank
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.debugging.ClassResolver
import po.misc.debugging.stack_tracer.TraceOptions
import po.misc.exceptions.error
import po.misc.types.safeCast
import po.misc.types.token.TokenizedResolver
import po.misc.types.token.TypeToken
import po.misc.types.token.filterTokenized
import po.misc.types.token.safeCast
import kotlin.collections.set

/**
 * A compiled render execution plan for a given receiver type [T] and rendered value type [V].
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
 * @param T the receiver (host) type driving rendering
 * @param V the value type rendered by elements in this plan
 */
class RenderPlan <S, T>(
    val host: TemplateHost<S, T>,
    internal val renderBacking: MutableMap<RenderKey, RenderNodeBase<T>> = mutableMapOf(),
): Component, ClassResolver, TokenizedResolver<S, T>{

    internal val journal = SimpleJournal(this::class)
    var onRender: ((RenderableElement<S, T>, values:  List<T>) -> String)? = null
    override val sourceType: TypeToken<S> get() =  host.sourceType
    override val receiverType:TypeToken<T> get() = host.receiverType

    val displayName: String = "RenderPlan<${sourceType.typeName}, ${receiverType.typeName}>"
    var verbosity: Verbosity = journal.verbosity

    private var orderCounter = -1
    val foreignNodes: MutableMap<RenderKey, RenderNodeBase<*>> = mutableMapOf<RenderKey,  RenderNodeBase<*>>()

    val renderMap : Map<RenderKey,  RenderNodeBase<T>> get() =  renderBacking
    val renderSize : Int get() = renderMap.values.sumOf { it.size }
    val size: Int get() = renderMap.size + foreignNodes.size
    val renderNodes: List<RenderNodeBase<*>> get() {
       return buildList {
            addAll(renderBacking.values)
            addAll(foreignNodes.values)
        }
    }
    override val componentName:String get() = "TemplateMap<${sourceType.typeName}, ${receiverType.typeName}>"
    init {
        if(foreignNodes.isNotEmpty() || renderMap.isNotEmpty()){
            orderCounter = foreignNodes.size + renderMap.size
        }
    }
    private val currentRender = mutableListOf<String>()
    private val expectedMsg =  "Expected Receiver: ${receiverType.typeName}"
    private val actualMsg : (RenderNodeBase<*>) -> String = {renderable-> "Actual: ${renderable.typeName}" }
    private val processingMsg: (RenderNodeBase<*>) -> String = {renderable-> "Processing: ${classInfo(renderable)}" }

    private fun addRender(value: String,  record: DataRecord? = null){
        ifUndefined(value){
            record?.warn("Render returned empty string")
        }
        currentRender.add(value)
    }
    private fun assembleRender():String{
        val result = currentRender.joinToStringNotBlank(SpecialChars.NEW_LINE)
        currentRender.clear()
        return result
    }
    private fun nextOrder(): Int = ++ orderCounter
    private fun getAllKeys(): List<RenderKey>{
       return buildList {
            addAll(renderMap.keys)
            addAll(foreignNodes.map { it.key})
        }.sortedBy { it.order }
    }

    @PublishedApi
    internal fun getNodesOrdered(): List<RenderNodeBase<*>>{
       return  renderNodes.sortedBy { it.order }
    }
    fun add(placeholder: TemplatePlaceholder<*>):PlaceholderNode<*>{
        val placeholder = PlaceholderNode(placeholder,  nextOrder())
        foreignNodes.putOverwriting(placeholder.key, placeholder){
            "Foreign nodes map key ${it.key} was overwritten by ${placeholder.key}".output(Colour.YellowBright)
        }
        return placeholder
    }
    fun add(valueGrid: PrettyValueGrid<T, *>):ValueGridNode<T, *>{
        val valueNode = ValueGridNode(valueGrid, nextOrder())
        renderBacking.putOverwriting(valueNode.key, valueNode){
            it.output("${valueNode.key} was overwritten by ${valueNode.key}")
        }
        return valueNode
    }
    fun add(valueRow: PrettyValueRow<T, *>): ValueRowNode<T>{
        val rowNode = ValueRowNode(valueRow, nextOrder())
        renderBacking.putOverwriting(rowNode.key, rowNode){
            it.output("${rowNode.key} was overwritten by ${rowNode.key}")
        }
        return rowNode
    }
    fun add(row: PrettyRow<T>): RowNode<T>{
        val rowNode = RowNode(row, nextOrder())
        renderBacking.putOverwriting(rowNode.key, rowNode){
            it.output("${rowNode.key} was overwritten by ${rowNode.key}")
        }
        return rowNode
    }
    fun add(key: RenderKey, renderNode:  RenderNodeBase<T>):RenderNodeBase<T>{
        renderBacking.putOverwriting(key, renderNode){
            it.output("$key was overwritten by $renderNode")
        }
        return renderNode
    }

    fun getJoinedRenderables(): List<RenderNodeBase<*>>{
        val ownRenderables = renderMap.values
        val foreignRenderables = foreignNodes.flatMap { getJoinedRenderables() }
        val joinedRenderables = buildList {
            addAll(ownRenderables)
            addAll(foreignRenderables)
        }
        return joinedRenderables.sortedBy { it.key.order }
    }

    fun populateBy(other: RenderPlan<S, T>){
        other.renderMap.forEach { (key, node) ->
            add(key, node)
        }
        other.foreignNodes.forEach {
            foreignNodes[it.key] = it.value
        }
    }
    fun copy(): RenderPlan<S, T> {
        val renderPlanCopy = RenderPlan(host)
        val copiedEnderMap = renderMap.values.map { it.copy() }
        copiedEnderMap.forEach {
            renderPlanCopy.renderBacking[it.key] = it
        }
        val copiedForeignMap = foreignNodes.values.map { it.copy() }
        copiedForeignMap.forEach {
            renderPlanCopy.foreignNodes[it.key] = it
        }
        renderPlanCopy.orderCounter = orderCounter
        return renderPlanCopy
    }
    fun clear(){
        renderBacking.clear()
        foreignNodes.values.forEach {it.clear()}
        foreignNodes.clear()
    }
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
    inline fun <reified E: TemplatePart<T2>, reified T2> getRenderable(key: RenderKey? = null): E? {
       return if(key != null){
           get(key).safeCast<E, T2>()
        }else{
            renderNodes.map { it.element }.filterTokenized<E, T2>().firstOrNull()
        }
    }

    fun render(receiver:T, opts: CommonRowOptions?):String{
        val renderKeys = getAllKeys()
        val renderSize = renderKeys.size
        val record =  journal.method("rendering: <${receiverType.typeName}>")
        for(i in 0 until renderSize){
            val key =  renderKeys[i]
            if(key.isOfSameTypeChain()){
                renderMap[key]?.let {node->
                   val render = node.renderFromSource(receiver, opts)
                   addRender(render, record)
                }
            }else{
                when (val renderable = foreignNodes[key]) {
                    is PlaceholderNode<*> -> {
                        if(renderable.enabled){
                            val render = renderable.render(opts)
                            addRender(render, record)
                        }
                    }
                    else -> error("Renderable $renderable should not be an element of ForeignMap", info(), TraceOptions.ThisMethod)
                }
            }
        }
        return assembleRender()
    }
    fun onRender(renderCallback : (RenderableElement<S, T>, values:  List<T>) -> String){
        onRender = renderCallback
    }
    fun info():RenderPlanSnapshot {
        val snp = RenderPlanSnapshot(this)
        return snp
    }
    override fun toString(): String = displayName
}
