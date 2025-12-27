package po.misc.data.pretty_print.parts.grid

import po.misc.callbacks.context_signal.ContextSignal
import po.misc.callbacks.context_signal.createContextSignal
import po.misc.collections.putOverwriting
import po.misc.context.component.Component
import po.misc.counters.DataRecord
import po.misc.counters.SimpleJournal
import po.misc.data.ifUndefined
import po.misc.data.output.output
import po.misc.data.pretty_print.Placeholder
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.TemplateHost
import po.misc.data.pretty_print.TemplatePart
import po.misc.data.pretty_print.TransitionGrid
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.strings.joinToStringNotBlank
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.debugging.ClassResolver
import po.misc.debugging.stack_tracer.TraceOptions
import po.misc.exceptions.checkNotNull
import po.misc.exceptions.error
import po.misc.functions.NoResult
import po.misc.functions.Throwing
import po.misc.reflection.primitives.StringClass
import po.misc.types.requireNotNull
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import po.misc.types.token.ifCasted
import po.misc.types.token.safeCast
import kotlin.collections.set
import kotlin.reflect.KClass

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
class RenderPlan <T, V>(
    val host: TemplateHost<T, V>,
    internal val renderBacking: MutableMap<RenderKey, RenderNodeBase<V, *>> = mutableMapOf(),
): Component, ClassResolver{

    var onRender: ((RenderableElement<T, V>, values:  List<V>) -> String)? = null
    val receiverType: TypeToken<T> get() =  host.receiverType
    val type:TypeToken<V> get() = host.valueType

    val displayName: String = "RenderPlan<${receiverType.typeName}, ${type.typeName}>"

    private val grdToken = TypeToken<PrettyRow<V>>()
    val rowRender : ContextSignal<PrettyRow<V>, String, Unit> = createContextSignal(grdToken, StringClass.typeToken, NoResult.token)
    internal val journal = SimpleJournal(this::class, immediateOutput = true)
    private var orderCounter = -1
    val foreignNodes: MutableList<RenderNodeBase<*, *>> = mutableListOf()
    val renderMap : Map<RenderKey,  RenderNodeBase<V, *>> get() =  renderBacking

    /**
     * Number of renderable elements owned directly by this plan.
     * Foreign render plans are not included.
     */
    val renderSize : Int get() = renderMap.values.sumOf { it.size }
    val size: Int get() = renderMap.size + foreignNodes.size
    val renderables: List<RenderableElement<*, *>> get() = renderBacking.values.map { it.element }.filterIsInstance<RenderableElement<*, *>>()

    /**
     * All [PrettyRow] elements declared directly in this plan.
     */
    val rows: List<PrettyRow<V>> get() = renderBacking.values.map{ it.element }.filterIsInstance<PrettyRow<V>>()
    override val componentName:String get() = "TemplateMap<${receiverType.typeName}, ${type.typeName}>"
    init {
        if(foreignNodes.isNotEmpty() || renderMap.isNotEmpty()){
            orderCounter = foreignNodes.size + renderMap.size
        }
    }
    private fun nextOrder(): Int = ++ orderCounter

    @PublishedApi
    internal fun getNodesOrdered(): List<RenderNodeBase<*, *>>{
       val list = buildList {
           addAll(foreignNodes)
           addAll(renderBacking.values)
       }
       return  list.sortedBy { it.order }
    }
    internal fun copyRenderMap():  MutableMap<RenderKey, RenderNodeBase<V, *>> {
        val mapCopy = mutableMapOf<RenderKey, RenderNodeBase<V, *>>()
        renderMap.forEach { (key, element) ->
            mapCopy[key.copy()] = element.copy()
        }
        return mapCopy
    }

    fun add(transition: TransitionGrid<*, *>):TransitionNode<*, *>{
        val transition = TransitionNode(transition,  nextOrder())
        foreignNodes.add(transition)
        return transition
    }
    fun add(valueGrid: PrettyValueGrid<V, *>):ValueGridNode<V, *>{
        val valueNode = ValueGridNode(valueGrid, nextOrder())
        renderBacking.putOverwriting(valueNode.key, valueNode){
            it.output("${valueNode.key} was overwritten by ${valueNode.key}")
        }
        return valueNode
    }
    fun add(row: PrettyRow<V>): RowNode<V>{
        val rowNode = RowNode(row, nextOrder())
        renderBacking.putOverwriting(rowNode.key, rowNode){
            it.output("${rowNode.key} was overwritten by ${rowNode.key}")
        }
        return rowNode
    }
    fun add(key: RenderKey, renderNode:  RenderNodeBase<V, *>):RenderNodeBase<V, *>{
        renderBacking.putOverwriting(key, renderNode){
            it.output("$key was overwritten by $renderNode")
        }
        return renderNode
    }
    fun getJoinedRenderables(): List<RenderNodeBase<*, *>>{
        val ownRenderables = renderMap.values
        val foreignRenderables = foreignNodes.flatMap { getJoinedRenderables() }
        val joinedRenderables = buildList {
            addAll(ownRenderables)
            addAll(foreignRenderables)
        }
        return joinedRenderables.sortedBy { it.key.order }
    }

    fun populateBy(other: RenderPlan<T, V>){
        other.renderMap.forEach { (key, node) ->
            add(key, node)
        }
        other.foreignNodes.forEach {
            foreignNodes.add(it)
        }
    }
    fun copy(): RenderPlan<T, V> {
        val renderPlan = RenderPlan(host,  copyRenderMap())
        val foreignCopy = foreignNodes.map { it.copy() }
        foreignNodes.addAll(foreignCopy)
        return renderPlan
    }
    fun clear(){
        renderBacking.clear()
        foreignNodes.forEach { it.clear() }
        foreignNodes.clear()
    }
    private fun getByOrder(order: Int): RenderNodeBase<*, *>?{
        val orderIndex = order.coerceAtLeast(0)
        val fakeKey = RenderKey(orderIndex, RenderableType.Any)
        val own = renderMap[fakeKey]
        if(own != null){
            return own
        }
       return foreignNodes.firstOrNull { it.order ==  orderIndex}
    }

    inline operator fun <reified E: TemplatePart<*, *>> get(kClass: KClass<E>): List<E> {
        val res = renderables.filterIsInstance<E>()
        return res
    }
    operator fun get(type: RenderableType): List<TemplatePart<*, *>> {
        return renderMap.values.filter { it.key.renderableType == type  }.map { it.element }
    }
    operator fun get(order: Int):  TemplatePart<*, *> {
       return getByOrder(order)?.element ?:run {
            error("Renderable with order number $order not found.", info(), TraceOptions.Method("get"))
        }
    }

    private val currentRender = mutableListOf<String>()
    private fun addRender(value: String,  record: DataRecord){
        ifUndefined(value){
            record.warn("Render returned empty string")
        }
        currentRender.add(value)
    }
    private fun assembleRender():String{
       val result = currentRender.joinToStringNotBlank(SpecialChars.NEW_LINE)
       currentRender.clear()
       return result
    }

    private val expectedMsg =  "Expected Receiver: ${type.typeName}"
    private val actualMsg : (RenderNodeBase<*, *>) -> String = {renderable-> "Actual: ${renderable.typeName}" }
    private val processingMsg: (RenderNodeBase<*, *>) -> String = {renderable-> "Processing: ${classInfo(renderable)}" }

    fun renderList(receiverList: List<V>, opts: CommonRowOptions? = null): String {
        val allNodes = getNodesOrdered()
        val count = allNodes.size
        val record =  journal.method("renderList: List<${receiverType.typeName}>")
        for (i in 0 until count) {
            when (val renderable = allNodes[i]) {
                is RowNode<*> -> {
                    record.addComment("Rendering $renderable $i of $count")
                    renderable.ifCasted<RowNode<V>, V>(type){
                        val render = render(receiverList, opts)
                        addRender(render, record)
                    }
                }
                is ValueGridNode<*, *> -> {
                   renderable.ifCasted<ValueGridNode<V, *>, V>(type){
                        val render = render(receiverList, opts)
                        addRender(render, record)
                   }
                }
                is TransitionNode<*, *> -> {
                    if(renderable.enabled){
                        val render = renderable.render(opts)
                        addRender(render, record)
                    }
                }
                is PlaceholderNode<*> -> {
                    if(renderable.enabled){
                        val render = renderable.render(opts)
                        addRender(render, record)
                    }
                }
            }
        }
        return assembleRender()
    }
    fun onRender(renderCallback : (RenderableElement<T, V>, values:  List<V>) -> String){
        onRender = renderCallback
    }
    fun info():RenderPlanSnapshot {
        val snp = RenderPlanSnapshot(this)
        return snp
    }
    override fun toString(): String = displayName
}
