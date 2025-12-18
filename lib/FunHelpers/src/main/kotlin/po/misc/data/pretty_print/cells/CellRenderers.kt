package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.types.token.TypeToken


interface StaticRender{
    fun render(): String
}

interface AnyRenderingCell{
    fun render(receiver: Any, commonOptions: CommonCellOptions? = null): String
}

interface ReceiverAwareCell<T: Any> {
    val typeToken: TypeToken<T>
    fun render(receiver:T, commonOptions: CommonCellOptions? = null): String
}