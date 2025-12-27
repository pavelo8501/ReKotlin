package po.misc.data.pretty_print

import po.misc.data.pretty_print.grid.ValueGridBuilder
import po.misc.data.pretty_print.grid.TransitionContainer
import po.misc.data.pretty_print.parts.loader.DataProvider
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1

@PublishedApi
internal inline fun <reified T, reified V> KProperty1<T, V>.toProvider(): DataProvider<T, V> = DataProvider<T, V>(this)

@PublishedApi
internal inline fun <T, reified V> KProperty1<T, V>.toProvider(
    typeToken: TypeToken<T>,
): DataProvider<T, V> {
   return  DataProvider<T, V>(typeToken, this)
}

@PublishedApi
internal fun <T, V> KProperty1<T, V>.toProvider(
    typeToken: TypeToken<T>,
    valueToken: TypeToken<V>
): DataProvider<T, V> {
    return  DataProvider<T, V>(typeToken, valueToken).also {
        it.resolveProperty(this)
    }
}


@PublishedApi
@JvmName("toProviderList")
internal fun <T, V> KProperty1<T, List<V>>.toProvider(
    typeToken: TypeToken<T>,
    valueToken: TypeToken<V>
): DataProvider<T, V> {
    return  DataProvider<T, V>(typeToken, valueToken).also {
        it.resolveListTypeProperty(this)
    }
}

internal fun  <T, V>  Function1<T, V>.toProvider(
    typeToken: TypeToken<T>,
    valueToken: TypeToken<V>
): DataProvider<T, V> {
   return DataProvider<T, V>(typeToken, valueToken).addResolver(this)
}

@PublishedApi
internal fun <T, V> DataProvider<T, V>.toValueGrid(options: RowOptions? = null): PrettyValueGrid<T, V>{

   val grid = PrettyValueGrid(this, )
    grid.dataLoader.applyCallables(this)
    if(options != null) {
        grid.options = options
    }
    return grid
}

fun <T, V> PrettyValueGrid<T, V>.toContainer(
    rowOptions: CommonRowOptions? = null
): ValueGridBuilder<T, V> {
    val gridCopy = copy()
    gridCopy.options =  PrettyHelper.toRowOptions(rowOptions, gridCopy.options)
    return ValueGridBuilder(dataProvider)
}

fun <T,  V> PrettyGrid<V>.toContainer(
    provider: DataProvider<T, V>,
    rowOptions: CommonRowOptions? = null
):TransitionContainer<T, V>{
    val gridCopy = copy()
    gridCopy.options =  PrettyHelper.toRowOptions(rowOptions, gridCopy.options)
    return TransitionContainer(provider, gridCopy)
}





