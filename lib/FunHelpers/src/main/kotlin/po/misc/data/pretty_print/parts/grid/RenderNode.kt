package po.misc.data.pretty_print.parts.grid

import po.misc.data.pretty_print.Placeholder
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.PrettyValueRow
import po.misc.data.pretty_print.TemplatePart
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.strings.appendLine
import po.misc.types.token.TokenizedResolver
import po.misc.types.token.TypeToken


sealed class RenderNodeBase<T>(
    val key: RenderKey,
    val element: TemplatePart<*>
){
    abstract val hasRenderPlan: Boolean
    open val size: Int = 1
    val order: Int get() = key.order
    val enabled: Boolean get() = element.enabled
    val typeName: String get() = "${element.receiverType.typeName},${element.receiverType.typeName}> "
    val renderableType: RenderableType get() = key.renderableType
    val templateIdText: String get() = element.templateID.formattedString
    val nodeInfo: String get() = buildString {
                appendLine("RenderNode ")
                appendLine("$templateIdText ")
                appendLine("$renderableType ")
            }

    abstract fun renderFromSource(source:T, opts: CommonRowOptions?):String

    override fun toString(): String = nodeInfo
    abstract fun copy(usingOptions: CommonRowOptions? = null): RenderNodeBase<T>
    abstract fun clear()
}

class RowNode<T>(
    val row: PrettyRow<T>,
    order: Int
):RenderNodeBase<T>(RenderKey(order, RenderableType.Row), row){

    override val hasRenderPlan:Boolean = false
    fun render(receiverList: List<T>, opts: CommonRowOptions? = null): String = row.render(receiverList, opts)

    override fun renderFromSource(source:T, opts: CommonRowOptions?):String = row.render(source, opts)
    override fun copy(usingOptions: CommonRowOptions? ):RowNode<T>{
        return RowNode(row.copy(usingOptions), order)
    }
    override fun clear(){

    }
}

class ValueGridNode<T, V>(
    val valueGrid: PrettyValueGrid<T, V>,
    order: Int
):RenderNodeBase<T>(RenderKey(order, RenderableType.ValueGrid), valueGrid){
    override val hasRenderPlan:Boolean = true
    override val size: Int get() =  valueGrid.renderPlan.size
    val renderPlan : RenderPlan<T, V> get() = valueGrid.renderPlan

    override fun renderFromSource(source: T, opts: CommonRowOptions?):String = valueGrid.renderFromSource(source, opts)
    override fun copy(usingOptions: CommonRowOptions? ):ValueGridNode<T, V>{
        return ValueGridNode(valueGrid.copy(), order)
    }
    override fun clear(): Unit = valueGrid.renderPlan.clear()
}

class ValueRowNode<T>(
    val valueRow: PrettyValueRow <T, *>,
    order: Int
):RenderNodeBase<T>(RenderKey(order, RenderableType.ValueRow), valueRow){
    override val hasRenderPlan:Boolean = true
    override fun renderFromSource(source:T, opts: CommonRowOptions?):String = valueRow.renderFromSource(source, opts)
    override fun copy(usingOptions: CommonRowOptions? ):ValueRowNode<T>{
        return ValueRowNode(valueRow.copy(), order)
    }
    override fun clear(): Unit {

    }
}

class PlaceholderNode<T>(
    val placeholder: Placeholder<T>,
    order: Int
):RenderNodeBase<T>(RenderKey(order, RenderableType.Placeholder), placeholder){
    override val hasRenderPlan:Boolean = true
    override val size: Int get() =  placeholder.renderPlan.size
    val renderPlan: RenderPlan<T, T> get() = placeholder.renderPlan

    fun render(opt: CommonRowOptions? = null): String = placeholder.render(opt)
    override fun renderFromSource(source:T, opts: CommonRowOptions?):String = placeholder.renderFromSource(source, opts)
    override fun copy(usingOptions: CommonRowOptions? ):PlaceholderNode<T>{
        return  PlaceholderNode(placeholder.copy(), order)
    }
    override fun clear(): Unit = placeholder.renderPlan.clear()
}
