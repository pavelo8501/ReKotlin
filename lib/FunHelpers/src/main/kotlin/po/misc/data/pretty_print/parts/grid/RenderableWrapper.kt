package po.misc.data.pretty_print.parts.grid

import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.RenderableElement
import po.misc.types.castOrThrow



class RenderableWrapper(
    val element: RenderableElement<*>,
    val type: RenderableType,
    val order: Int,
){
    fun asForeignGrid(): PrettyGrid<*> = element.castOrThrow<PrettyGrid<*>>()
    fun <T: Any> asGrid(): PrettyGrid<T> = element.castOrThrow<PrettyGrid<T>>()
    fun <T: Any> asValueGrid(): PrettyValueGrid<T, *> = element.castOrThrow<PrettyValueGrid<T, *>>()
    fun <T: Any> asRow(): PrettyRow<T> = element.castOrThrow<PrettyRow<T>>()
    override fun toString(): String = element.toString()
}