package po.misc.data.pretty_print.grid

import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.PrettyValueGrid
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.parts.loader.DataProvider
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.template.GridID
import po.misc.data.pretty_print.toProvider
import po.misc.types.castOrThrow
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1

@PublishedApi
internal fun <T: Any> buildGrid(
    token :  TypeToken<T>,
    rowOptions: CommonRowOptions? = null,
    provider: (() -> T)? = null,
    listProvider: (() -> List<T>)? = null,
    builder: HostGridBuilder<T>.() -> Unit
): PrettyGrid<T> {
    val options = PrettyHelper.toRowOptions(rowOptions)
    val container = HostGridBuilder(token)
    container.setProviders(provider, listProvider)
    builder.invoke(container)
    val grid =  container.finalizeGrid(null)
    return grid
}

inline fun <reified T: Any> buildPrettyGrid(
    gridID: GridID? = null,
    noinline builder: HostGridBuilder<T>.() -> Unit
): PrettyGrid<T> {
    val container = HostGridBuilder(TypeToken.create<T>(), gridID)
    builder.invoke(container)
    return container.finalizeGrid(null)
}

inline fun <reified T, reified V> prepareValueGrid(
    property: KProperty1<T, V>,
    gridID: GridID? = null,
    noinline  builder: ValueGridBuilder<T, V>.() -> Unit
): ValueGridBuilder<T, V> {
    val provider = property.toProvider<T,V>()
    val container = ValueGridBuilder(
        provider,
        gridID
    )
    container.preSaveBuilder(builder)
    return container
}

inline fun <reified T, reified V> prepareListGrid(
    property: KProperty1<T,  List<V>>,
    gridID: GridID? = null,
    noinline  builder: ValueGridBuilder<T, V>.() -> Unit
): ValueGridBuilder<T, V> {
    val provider = property.toProvider(TypeToken<T>(), TypeToken<V>())
    val container = ValueGridBuilder(
        provider,
        gridID
    )
    container.preSaveBuilder(builder)
    return container
}


@Deprecated("Remove")
@PublishedApi
internal fun <T: Any, V> buildValueGrid(
    hostTypeToken:  TypeToken<T>,
    token :  TypeToken<V>,
    rowOptions: CommonRowOptions?,
    property: KProperty1<T, V>? = null,
    provider: (() -> V)? = null,
    listProvider: (() -> List<V>)? = null,
    builder: ValueGridBuilder<T, V>.() -> Unit
): PrettyValueGrid<T, V> {
    val options = PrettyHelper.toRowOptions(rowOptions)

    val container = when{
        property != null ->{
            val provider =  DataProvider(hostTypeToken, token).also {
                it.resolveProperty(property)
            }
            ValueGridBuilder(provider)
        }
        provider != null ->{
            val provider =  DataProvider(hostTypeToken, token).also {
                it.addProvider(provider)
            }
            ValueGridBuilder(provider)
        }
       else ->{ error("Provider is not defined!") }
    }

    builder.invoke(container)
    return  container.finalizeGrid().castOrThrow()
}
