package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.types.token.TypeToken


inline fun <reified T: Templated<T>> T.buildRowContainer(
    rowOptions: CommonRowOptions? = null,
    builder: RowContainer<T>.()-> Unit
): RowContainer<T> {
    val token = TypeToken.create<T>()
    val container = RowContainer<T>(token, PrettyHelper.toRowOptions(rowOptions))
    container.singleLoader.setProvider{
        this
    }
    builder.invoke(container)
    return container
}

inline fun <reified T: Templated<T>> List<T>.buildRowContainer(
    rowOptions: CommonRowOptions? = null,
    builder: RowContainer<T>.()-> Unit
): RowContainer<T> {
    val token = TypeToken.create<T>()
    val container = RowContainer<T>(token, PrettyHelper.toRowOptions(rowOptions))
    container.listLoader.setProvider{
        this
    }
    builder.invoke(container)
    return container
}