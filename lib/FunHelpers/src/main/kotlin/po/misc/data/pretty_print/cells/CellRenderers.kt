package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.options.CommonCellOptions
import po.misc.data.pretty_print.parts.options.Options
import po.misc.types.castOrThrow
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import po.misc.types.token.safeCast


sealed interface RenderableCell<T>{
    val cellOptions: Options
}

sealed interface AnyRenderingCell: RenderableCell<String> {
    fun render(content: Any, commonOptions: CommonCellOptions? = null): String
}

sealed interface StaticRender: RenderableCell<String>,  AnyRenderingCell{
    fun render(commonOptions: CommonCellOptions? = null): String
}

sealed interface ReceiverAwareCell<T> : RenderableCell<T>{
    val receiverType: TypeToken<T>
    fun render(receiver:T, commonOptions: CommonCellOptions? = null): String
}
