package po.misc.data.pretty_print.grid

import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.properties.isReturnTypeList
import po.misc.types.token.TokenOptions
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1

@PublishedApi
internal fun <T: Any> buildGrid(
    token :  TypeToken<T>,
    rowOptions: CommonRowOptions? = null,
    provider: (() -> T)? = null,
    listProvider: (() -> List<T>)? = null,
    builder: GridContainer<T>.() -> Unit
): PrettyGrid<T> {
    val options = PrettyHelper.toRowOptions(rowOptions)
    val container = GridContainer(token, options)
    container.setProviders(provider, listProvider)
    val grid = container.applyBuilder(builder)
    return grid
}

inline fun <reified T: Any> buildPrettyGrid(
    rowOptions: CommonRowOptions? = null,
    noinline builder: GridContainer<T>.() -> Unit
): PrettyGrid<T> = buildGrid(TypeToken.create<T>(), rowOptions, builder = builder)

inline fun <reified T: Any> buildPrettyGrid(
    rowId: Enum<*>,
    noinline builder: GridContainer<T>.() -> Unit
): PrettyGrid<T> = buildGrid(TypeToken.create<T>(), RowOptions(rowId), builder = builder)

inline fun <reified T: Any> buildPrettyGrid(
    orientation: Orientation,
    rowId: Enum<*>? = null,
    noinline builder: GridContainer<T>.() -> Unit
): PrettyGrid<T> = buildGrid(TypeToken.create<T>(), RowOptions(orientation, rowId), builder = builder)

inline fun <reified T: Templated> T.buildGridForContext(
    rowOptions: CommonRowOptions? = null,
    noinline builder: GridContainer<T>.() -> Unit
): PrettyGrid<T> = buildGrid(TypeToken.create<T>(), rowOptions, provider = { this }, builder =  builder)


inline fun <reified T: Templated>  List<T>.buildGridForContext(
    rowOptions: CommonRowOptions? = null,
    noinline builder: GridContainer<T>.() -> Unit
): PrettyGrid<T> = buildGrid(TypeToken.create<T>(), rowOptions, listProvider = { this }, builder = builder)


@PublishedApi
internal fun <T: Any, V: Any> buildValueGrid(
    hostTypeToken:  TypeToken<T>,
    token :  TypeToken<V>,
    rowOptions: CommonRowOptions?,
    property: KProperty1<T, V>? = null,
    listProperty: KProperty1<T, List<V>>? = null,
    provider: (() -> V)? = null,
    listProvider: (() -> List<V>)? = null,
    builder: GridValueContainer<T, V>.() -> Unit
): PrettyValueGrid<T, V> {
    val options = PrettyHelper.toRowOptions(rowOptions)
    val container = GridValueContainer(hostTypeToken, token, options)
    container.setProperties(property, listProperty)
    container.setProviders(provider, listProvider)
    return  container.applyBuilder(builder)
}

inline fun <reified T: Any, reified V: Any> buildPrettyGrid(
    property: KProperty1<T, V>,
    rowOptions: CommonRowOptions? = null,
    noinline builder: GridValueContainer<T, V>.() -> Unit
): PrettyValueGrid<T, V> = buildValueGrid(TypeToken.create<T>(), TypeToken.create<V>(), rowOptions, property = property, builder = builder)



inline fun <reified T: Any, reified V: Any> buildPrettyGrid2(
    property: KProperty1<T, V>,
    rowOptions: CommonRowOptions? = null,
    noinline builder: GridValueContainer<T, V>.() -> Unit
): GridValueContainer<T, V> {
    val options = PrettyHelper.toRowOptions(rowOptions)
    val hostType = TypeToken.create<T>()
    val type = if(property.isReturnTypeList){
        TypeToken<V>(TokenOptions.ListType)
    }else{
        TypeToken<V>()
    }
    val container = GridValueContainer(hostType, type, options)
    container.setProperty(property)
    builder.invoke(container)
    return container
}



inline fun <reified T: Templated, reified V: Any> T.buildGridForContext(
    noinline provider: ()-> V,
    noinline builder: GridValueContainer<T, V>.() -> Unit
): PrettyValueGrid<T, V> {
    val grid = buildValueGrid(TypeToken.create<T>(), TypeToken.create<V>(), RowOptions(), provider = provider , builder =  builder)
    return grid
}

inline fun <reified T: Any, V: List<VT>, reified VT: Any> buildPrettyGridList(
    property: KProperty1<T, V>,
    rowOptions: CommonRowOptions? = null,
    noinline builder: GridValueContainer<T, VT>.() -> Unit
): PrettyValueGrid<T, VT> = buildValueGrid(TypeToken.create<T>(), TypeToken.create<VT>(), rowOptions,  listProperty = property, builder = builder)
