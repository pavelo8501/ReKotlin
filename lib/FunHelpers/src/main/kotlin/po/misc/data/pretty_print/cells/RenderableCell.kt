package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.options.CellOptions
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.rendering.CellParameters
import po.misc.types.token.TypeToken

sealed interface RenderableCell<T>{
    var currentRenderOpts: Options
    val sourceType: TypeToken<T>
    val keyText: String?
    fun CellParameters.scopedRender(receiver: T): RenderRecord
}

sealed interface StaticRenderingCell: RenderableCell<Unit>{
    override val sourceType: TypeToken<Unit> get() = TypeToken<Unit>()
    fun render(opts: CellOptions? = null): String
}

sealed interface AnyRenderingCell: RenderableCell<Any>{
    override val sourceType: TypeToken<Any> get() =  TypeToken<Any>()
    fun render(content: Any, opts: CellOptions? = null): String
}

sealed interface SourceAwareCell<S> : RenderableCell<S>{
    fun render(source:S, opts: CellOptions? = null): String
}

interface SourceLessCell