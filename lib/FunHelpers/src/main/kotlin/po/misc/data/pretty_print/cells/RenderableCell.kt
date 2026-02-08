package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.options.CellOptions
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.render.CellParameters
import po.misc.data.text_span.TextSpan
import po.misc.types.token.TypeToken

sealed interface RenderableCell{
    var renderOptions: Options
    val keyText: String?

    fun copy(): RenderableCell
}

sealed interface StaticRenderingCell: RenderableCell{
    val sourceType: TypeToken<Unit> get() = TypeToken<Unit>()
    fun CellParameters.renderInScope(): TextSpan
    fun render(opts: CellOptions? = null): String
}

sealed interface AnyRenderingCell: RenderableCell{
    val sourceType: TypeToken<Any> get() =  TypeToken<Any>()
    fun CellParameters.renderInScope(receiver: Any): TextSpan
    fun render(content: Any, opts: CellOptions? = null): String
}

sealed interface SourceAwareCell<S> : RenderableCell{
    val sourceType: TypeToken<S>
    fun CellParameters.renderInScope(receiver: S): TextSpan
    fun render(source:S, opts: CellOptions? = null): String
}