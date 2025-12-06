package po.misc.data.pretty_print.grid
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.ValueLoader
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1

inline fun <reified T: Any> buildPrettyGrid(
    rowOptions: CommonRowOptions? = null,
    noinline builder: RowContainer<T>.() -> Unit
): PrettyGrid<T>{
    val options = PrettyHelper.toRowOptions(rowOptions)
    val token = TypeToken.create<T>()
    val container = RowContainer(token, options)
    return container.buildGrid(builder)
}


inline fun <reified T: Any, reified V: Any> buildPrettyGrid(
    property: KProperty1<T, V>,
    rowOptions: CommonRowOptions? = null,
    noinline builder: RowValueContainer<T, V>.() -> Unit
):  PrettyValueGrid<T, V> {
    val options = PrettyHelper.toRowOptions(rowOptions)
    val container = RowValueContainer( TypeToken.create<T>(),  TypeToken.create<V>(), options)
    container.buildGrid(property, builder)
    return container.valueGrid
}



inline fun <reified T: Any, V: List<VT>, reified VT: Any> buildPrettyGridList(
    property: KProperty1<T, V>,
    rowOptions: CommonRowOptions? = null,
    noinline builder: RowValueContainer<T, VT>.() -> Unit
):  PrettyValueGrid<T, VT>{
    val options = PrettyHelper.toRowOptions(rowOptions)
    val token = TypeToken.create<T>()
    val valueToken = TypeToken.create<VT>()
    val container = RowValueContainer(token, valueToken, options)
    return container.buildGrid(property, builder)
}


inline fun <reified T: Any,  reified V: Templated> V.buildPrettyGrid(
    grid: PrettyGrid<T>,
    rowOptions: CommonRowOptions? = null,
    noinline builder: RowValueContainer<T, V>.(ValueLoader<T, V>) -> Unit
):  PrettyValueGrid<T, V>{
    val options = PrettyHelper.toRowOptions(rowOptions)
    val token = TypeToken.create<V>()
    val container = RowValueContainer(grid.typeToken,  token,  options)
    val provider = { this }
    return container.buildGrid(provider, builder)
}



