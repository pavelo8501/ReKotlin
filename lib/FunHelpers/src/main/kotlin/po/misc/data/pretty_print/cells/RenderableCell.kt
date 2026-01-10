package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.options.CellOptions
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.rendering.CellRenderParameters
import po.misc.data.pretty_print.parts.rendering.RenderParameters
import po.misc.types.token.TypeToken

sealed interface RenderableCell<T>{
    val currentRenderOpts: Options

    val sourceType: TypeToken<T>
    val keyText: String?
    val keySize : Int get() = keyText?.length?:0
    val keySegmentSize: Int get() {
        if(keyText.isNullOrBlank()){
            return 0
        }
        return keySize + currentRenderOpts.keySeparator.size
    }
    fun parametrizeRender(renderParameters: CellRenderParameters)
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