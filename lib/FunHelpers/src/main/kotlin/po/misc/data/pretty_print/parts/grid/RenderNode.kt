package po.misc.data.pretty_print.parts.grid

import po.misc.data.pretty_print.Placeholder
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.PrettyValueRow
import po.misc.data.pretty_print.TemplatePart
import po.misc.data.pretty_print.parts.common.RenderData
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.render.RenderCanvas
import po.misc.data.pretty_print.parts.render.RenderParameters

sealed interface RenderNode<T>{

    val index: Int
    val key: RenderKey
    val templateIdText: String
    val renderableType: RenderableType
    val enabled: Boolean
    val element: TemplatePart<*>
    val size: Int
    val sourceWidth: Int

    fun renderFromSource(source:T, opts: CommonRowOptions?):String
    fun scopedRender(source: T): RenderCanvas
    fun copy(usingOptions: CommonRowOptions? = null): RenderNode<T>
}

sealed interface SelfContainedNode<T> : RenderNode<T>{
    fun render(opt: CommonRowOptions? = null): String
    override fun renderFromSource(source: T, opts: CommonRowOptions?): String {
        return render(opts)
    }
    fun clear()
}

sealed class RenderNodeBase<T>(
    override val element: TemplatePart<*>,
    index: Int,
    override val renderableType: RenderableType,
): RenderNode<T>{

    override val key: RenderKey = RenderKey(index, renderableType)
    override val size:Int = 1
    open  val hasRenderPlan: Boolean = false
    override val enabled: Boolean get() = element.enabled
    final override val sourceWidth: Int get() = element.keyParameters.contentWidth

    val typeName: String get() = "${element.receiverType.typeName},${element.receiverType.typeName}> "
    override val templateIdText: String get() = element.templateID.formattedString
    val nodeInfo: String get() = buildString {
        appendLine("RenderNode ")
        appendLine("$templateIdText ")
        appendLine("$renderableType ")
    }

    abstract override fun renderFromSource(source:T, opts: CommonRowOptions?):String
    abstract override fun scopedRender(source: T): RenderCanvas

    override fun toString(): String = nodeInfo
    abstract override fun copy(usingOptions: CommonRowOptions? ): RenderNodeBase<T>
    abstract fun clear()
}

class RowNode<T>(
    val host: RenderPlan<*, T>,
    val row: PrettyRow<T>,
    override val index: Int
):RenderNodeBase<T>(row, index, RenderableType.Row), RenderNode<T>, RenderParameters by host {

    init {
        row.planner.keyParams.index = index
    }

    override fun renderFromSource(source:T, opts: CommonRowOptions?):String = row.render(source, opts)
    override fun scopedRender(source: T): RenderCanvas = with(row){ renderInScope(source) }
    override fun copy(usingOptions: CommonRowOptions? ):RowNode<T> =
        RowNode(host, row.copy(usingOptions), index)
    override fun clear(): Unit = row.planner.clear()
}

class ValueRowNode<T>(
    val host: RenderPlan<*, T>,
    val valueRow: PrettyValueRow <T, *>,
    override val index: Int
):RenderNodeBase<T>(valueRow, index, RenderableType.ValueRow), RenderNode<T>, RenderParameters by host {

    init {
        valueRow.planner.keyParams.index = index
    }
    override fun renderFromSource(source:T, opts: CommonRowOptions?):String = valueRow.renderFromSource(source, opts)
    override fun scopedRender(source: T): RenderCanvas = with(valueRow){ renderInScope(source) }
    override fun copy(usingOptions: CommonRowOptions? ):ValueRowNode<T> =
        ValueRowNode(host,  valueRow.copy(), index)
    override fun clear(): Unit = valueRow.planner.clear()
}

class ValueGridNode<T, V>(
    val host: RenderPlan<*, T>,
    val valueGrid: PrettyValueGrid<T, V>,
    override val index: Int
):RenderNodeBase<T>(valueGrid,  index , RenderableType.ValueGrid), RenderNode<T>, RenderParameters by host{

    val renderPlan : RenderPlan<T, V> get() = valueGrid.renderPlan
    override val size:Int get() = renderPlan.size
    override val hasRenderPlan:Boolean = true

    init {
        valueGrid.renderPlan.keyParams.index = index
    }

    override fun renderFromSource(source: T, opts: CommonRowOptions?):String = valueGrid.renderFromSource(source, opts)
    override fun scopedRender(source: T): RenderCanvas = with(valueGrid){ renderInScope(source) }

    override fun copy(usingOptions: CommonRowOptions? ):ValueGridNode<T, V> =
        ValueGridNode(host, valueGrid.copy(), index)
    override fun clear(): Unit = valueGrid.renderPlan.clear()
}

class PlaceholderNode<T>(
    val host: RenderPlan<*, *>,
    val placeholder: Placeholder<T>,
    override val index: Int
):RenderNodeBase<T>(placeholder, index, RenderableType.Placeholder), SelfContainedNode<T>, RenderParameters by host{

    val renderPlan: RenderPlan<T, T> get() = placeholder.renderPlan
    override val size:Int get() = renderPlan.size
    override val hasRenderPlan:Boolean = true

    override fun render(opt: CommonRowOptions?): String = placeholder.render(opt)
    override fun renderFromSource(source:T, opts: CommonRowOptions?):String = placeholder.renderFromSource(source, opts)
    override fun scopedRender(source: T): RenderCanvas = placeholder.renderInScope(this)

    override fun copy(usingOptions: CommonRowOptions? ):PlaceholderNode<T> =
        PlaceholderNode(host, placeholder.copy(), index)

    override fun clear(): Unit = placeholder.renderPlan.clear()
}
