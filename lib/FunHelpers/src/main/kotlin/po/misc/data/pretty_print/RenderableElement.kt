package po.misc.data.pretty_print

import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.RowID
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.types.token.TypeToken


sealed interface RenderableElement<T: Any> {

    val options: RowOptions
    val enabled: Boolean
    val hostType: TypeToken<T>

    val id: RowID? get() {
        val nullableOptions : RowOptions? = options as RowOptions?
        if(nullableOptions != null) {
            return nullableOptions.rowId
        }else{
            return null
        }
    }

    fun renderOnHost(host: T, opts: CommonRowOptions?): String
    override fun toString(): String

    fun shouldRender():Boolean{
        return enabled
    }

}
