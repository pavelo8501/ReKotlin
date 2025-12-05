package po.misc.data.pretty_print.grid
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1



inline fun <reified T: Any> buildPrettyGrid(
    rowOptions: CommonRowOptions? = null,
    noinline builder: RowContainer<T>.() -> Unit
): PrettyGrid<T>{
    val options = PrettyHelper.toRowOptionsOrDefault(rowOptions)
    val token = TypeToken.create<T>()
    val container = RowContainer(token, options)
    return container.buildGrid(builder)
}

inline fun <reified T: Any, V: List<VT>, reified VT: Any> buildPrettyGridList(
    property: KProperty1<T, V>,
    rowOptions: CommonRowOptions? = null,
    noinline builder: RowValueContainer<T, VT>.() -> Unit
):  PrettyValueGrid<T, VT>{
    val options = PrettyHelper.toRowOptionsOrDefault(rowOptions)
    val token = TypeToken.create<T>()
    val valueToken = TypeToken.create<VT>()
    val container = RowValueContainer(token, valueToken, options)
    return container.buildGrid(property, builder)
}

inline fun <reified T: Any, reified V: Any> buildPrettyGrid(
    property: KProperty1<T, V>,
    rowOptions: CommonRowOptions? = null,
    noinline builder: RowValueContainer<T, V>.() -> Unit
):  PrettyValueGrid<T, V>{
    val options = PrettyHelper.toRowOptionsOrDefault(rowOptions)
    val token = TypeToken.create<T>()
    val valueToken = TypeToken.create<V>()
    val container = RowValueContainer(token, valueToken, options)
    return container.buildGrid(property, builder)
}