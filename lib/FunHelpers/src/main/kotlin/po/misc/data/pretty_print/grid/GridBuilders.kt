package po.misc.data.pretty_print.grid

import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.PrettyDSL
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowID
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.properties.isReturnTypeList
import po.misc.types.token.TokenOptions
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty
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
    rowOptions: CommonRowOptions,
    noinline builder: GridContainer<T>.() -> Unit
): PrettyGrid<T> = buildGrid(TypeToken.create<T>(), rowOptions, builder = builder)


inline fun <reified T: Any> buildPrettyGrid(
    rowId: RowID? = null,
    noinline builder: GridContainer<T>.() -> Unit
): PrettyGrid<T> {
    val container = GridContainer(TypeToken.create<T>(), RowOptions().useId(rowId) )
    return container.applyBuilder(builder)
}

inline fun <reified T: Any> buildPrettyGrid(
    orientation: Orientation,
    rowId: RowID? = null,
    noinline builder: GridContainer<T>.() -> Unit
): PrettyGrid<T> = buildGrid(TypeToken.create<T>(), RowOptions(orientation, rowId), builder = builder)


inline fun <reified T: Templated<T>> T.buildGridForContext(
    rowOptions: CommonRowOptions? = null,
    noinline builder: GridContainer<T>.() -> Unit
): PrettyGrid<T> = buildGrid(TypeToken.create<T>(), rowOptions, provider = { this }, builder =  builder)


inline fun <reified T: Templated<T>>  List<T>.buildGridForContext(
    rowOptions: CommonRowOptions? = null,
    noinline builder: GridContainer<T>.() -> Unit
): PrettyGrid<T> = buildGrid(TypeToken.create<T>(), rowOptions, listProvider = { this }, builder = builder)


@PublishedApi
internal fun <T: Any, V: Any> buildValueGrid(
    hostTypeToken:  TypeToken<T>,
    token :  TypeToken<V>,
    rowOptions: CommonRowOptions?,
    property: KProperty1<T, V>? = null,
    provider: (() -> V)? = null,
    listProvider: (() -> List<V>)? = null,
    builder: GridValueContainer<T, V>.() -> Unit
): PrettyValueGrid<T, V> {
    val options = PrettyHelper.toRowOptions(rowOptions)
    val container = if(property != null){
        GridValueContainer(hostTypeToken, token, property, options =  options)
    }else{
        val cont =   GridValueContainer(hostTypeToken, token, options)
        cont.setProviders(provider, listProvider) as GridValueContainer
    }
    return  container.applyBuilder(builder)
}


inline fun <reified T: Any, reified V: Any> buildPrettyGrid(
    property: KProperty1<T, V>,
    rowOptions: CommonRowOptions? = null,
    noinline builder: GridValueContainer<T, V>.() -> Unit
): PrettyValueGrid<T, V> {
    val type = if(property.isReturnTypeList){
        TypeToken<V>(TokenOptions.ListType)
    }else{
        TypeToken<V>()
    }
    return buildValueGrid(TypeToken.create<T>(), type, rowOptions, property = property, builder = builder)
}


inline fun <reified T: Any, reified V: Any> buildPrettyListGrid(
    propertyList: KProperty1<T,  List<V>>,
    rowOptions: CommonRowOptions? = null,
    noinline builder: GridValueContainer<T, V>.() -> Unit
): PrettyValueGrid<T, V> {
    val type = TypeToken<V>(TokenOptions.ListType)
    val options = PrettyHelper.toRowOptions(rowOptions)
    val container = GridValueContainer(TypeToken.create<T>(), type, listProperty =  propertyList, options =  options)
    return  container.applyBuilder(builder)
}


inline fun <reified T: Templated<T>, reified V: Any> T.buildGridForContext(
    noinline provider: ()-> V,
    noinline builder: GridValueContainer<T, V>.() -> Unit
): PrettyValueGrid<T, V> {
    val grid = buildValueGrid(TypeToken.create<T>(), TypeToken.create<V>(), RowOptions(), provider = provider , builder =  builder)
    return grid
}