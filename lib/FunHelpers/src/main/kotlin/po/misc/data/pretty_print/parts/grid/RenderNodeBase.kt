package po.misc.data.pretty_print.parts.grid

import po.misc.data.pretty_print.Placeholder
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.TemplatePart
import po.misc.data.pretty_print.TransitionGrid
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.strings.appendLine
import po.misc.types.token.TokenizedResolver
import po.misc.types.token.TypeToken


sealed class RenderNodeBase<T, V>(
    val key: RenderKey,
    val element: TemplatePart<T, V>
): TokenizedResolver<T, V>  by element {

    abstract val hasRenderPlan:Boolean
    abstract override val typeToken: TypeToken<T>
    open val size: Int = 1
    val order:Int get() = key.order
    val enabled : Boolean get() = element.enabled
    val typeName:String get() =  "${receiverType.typeName},${valueType.typeName}> "
    val renderableType: RenderableType get() = key.renderableType
    val templateIdText: String  get() = element.id.formattedString

    val nodeInfo: String get() {
        return buildString {
            appendLine("RenderNode ")
            appendLine("$templateIdText ")
            appendLine("$renderableType ")
        }
    }
    override fun toString(): String = nodeInfo
    abstract fun copy(usingOptions: CommonRowOptions? = null):RenderNodeBase<T, V>
    abstract fun clear()
}

class RowNode<T>(
    val row: PrettyRow<T>,
    order: Int
):RenderNodeBase<T, T>(RenderKey(order, RenderableType.Row), row){

    override val hasRenderPlan:Boolean = false
    override val typeToken: TypeToken<T> get() = row.receiverType

    fun render(receiverList: List<T>, opts: CommonRowOptions? = null): String = row.render(receiverList, opts)
    override fun copy(usingOptions: CommonRowOptions? ):RowNode<T>{
        return RowNode(row.copy(usingOptions), order)
    }
    override fun clear(){

    }
}

class ValueGridNode<T, V>(
    val valueGrid: PrettyValueGrid<T, V>,
    order: Int
):RenderNodeBase<T, V>(RenderKey(order, RenderableType.ValueGrid), valueGrid){
    override val hasRenderPlan:Boolean = true
    override val size: Int get() =  valueGrid.renderPlan.size
    override val typeToken: TypeToken<T> get() = valueGrid.receiverType
    val renderPlan : RenderPlan<T, V> get() = valueGrid.renderPlan

    fun render(receiverList: List<T>, opts: CommonRowOptions? = null): String = valueGrid.render(receiverList, opts)

    override fun copy(usingOptions: CommonRowOptions? ):ValueGridNode<T, V>{
        return ValueGridNode(valueGrid.copy(), order)
    }
    override fun clear(): Unit = valueGrid.renderPlan.clear()

}

class TransitionNode<T, V>(
    val transitionGrid: TransitionGrid<T, V>,
    order: Int
):RenderNodeBase<T, V>(RenderKey(order, RenderableType.Transition), transitionGrid){
    override val hasRenderPlan:Boolean = true
    override val size: Int get() =  transitionGrid.renderPlan.size
    override val typeToken: TypeToken<T> get() = transitionGrid.receiverType
    val renderPlan: RenderPlan<T, V> get() = transitionGrid.renderPlan

    fun render(receiverList: List<T>, opts: CommonRowOptions? = null): String = transitionGrid.render(receiverList, opts)
    fun render(opt: CommonRowOptions? = null): String = transitionGrid.render(opt)

    override fun copy(usingOptions: CommonRowOptions? ):TransitionNode<T, V>{
        return TransitionNode(transitionGrid.copy(), order)
    }
    override fun clear(): Unit = transitionGrid.renderPlan.clear()
    override fun toString(): String  = nodeInfo
}

class PlaceholderNode<T>(
    val placeholder: Placeholder<T>,
    order: Int
):RenderNodeBase<T, T>(RenderKey(order, RenderableType.Placeholder), placeholder){

    override val hasRenderPlan:Boolean = true
    override val size: Int get() =  placeholder.renderPlan.size
    override val typeToken: TypeToken<T> get() = placeholder.receiverType
    val renderPlan: RenderPlan<T, T> get() = placeholder.renderPlan

    fun render(opt: CommonRowOptions? = null): String = placeholder.render(opt)
    fun render(receiverList: List<T>, opts: CommonRowOptions? = null): String = placeholder.render(receiverList, opts)

    override fun copy(usingOptions: CommonRowOptions? ):PlaceholderNode<T>{
        return  PlaceholderNode(placeholder.copy(), order)
    }
    override fun clear(): Unit = placeholder.renderPlan.clear()
}