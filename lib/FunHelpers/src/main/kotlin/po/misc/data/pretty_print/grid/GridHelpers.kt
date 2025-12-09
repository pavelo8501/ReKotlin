package po.misc.data.pretty_print.grid

import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.rows.copyRow
import po.misc.types.castOrThrow
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1




private fun <T: Any, V: Any> toValueGridConverter(
   grid: PrettyGrid<V>,
   hostTypeToken: TypeToken<T>,
   opt: RowOptions? = null,
   property: KProperty1<T, V>? = null,
   listProperty: KProperty1<T, List<V>>? = null,
   provider: (() -> V)? = null,
   listProvider:  (() -> List<V>)? = null,
): PrettyValueGrid<T, V>{

    val copied = grid.rows.map { it.copyRow(grid.typeToken) }
    val valueGrid = PrettyValueGrid(hostTypeToken, grid.typeToken, opt?:grid.options)
    valueGrid.addRows(copied)
    if(property != null){
        valueGrid.singleLoader.setReadOnlyProperty(property)
    }
    if(listProperty != null){
        valueGrid.listLoader.setReadOnlyProperty(listProperty)
    }
    if(provider != null){
        valueGrid.singleLoader.setProvider(provider)
    }
    if(listProvider != null){
        valueGrid.listLoader.setProvider(listProvider)
    }
    return valueGrid
}

fun <T: Any, V: Any> PrettyGrid<V>.toValueGrid(
    hostTypeToken: TypeToken<T>,
    property: KProperty1<T, V>,
    opt: RowOptions? = null
): PrettyValueGrid<T, V> = toValueGridConverter(this, hostTypeToken, opt,  property= property)

fun <T: Any, V: Any> PrettyGrid<V>.toValueGridList(
    hostTypeToken: TypeToken<T>,
    property: KProperty1<T, List<V>>,
    opt: RowOptions? = null
): PrettyValueGrid<T, V>  = toValueGridConverter(this, hostTypeToken, opt, listProperty = property)

fun <T: Any, V: Any> PrettyGrid<V>.toValueGrid(
    hostTypeToken: TypeToken<T>,
    provider: () -> V,
    opt: RowOptions? = null
): PrettyValueGrid<T, V> = toValueGridConverter(this, hostTypeToken, opt,  provider = provider)


fun <T: Any, V: Any> PrettyGrid<V>.toValueGridList(
    hostTypeToken: TypeToken<T>,
    provider: () -> List<V>,
    opt: RowOptions? = null
): PrettyValueGrid<T, V>  = toValueGridConverter(this, hostTypeToken, opt, listProvider = provider)



fun <T: Any, V: Any> PrettyValueGrid<T, V>.copy(
    opt: RowOptions? = null
): PrettyValueGrid<T, V> {
    val newGrid = PrettyValueGrid(hostTypeToken, typeToken, opt?:options)
    newGrid.singleLoader.initFrom(singleLoader)
    newGrid.listLoader.initFrom(listLoader)
    newGrid.addRows(rows)
    return newGrid
}


fun <T: Any, V: Any> PrettyGrid<V>.copy(
    hostTypeToken: TypeToken<T>,
    opt: RowOptions? = null
):PrettyValueGrid<T, V>{
   val casted = castOrThrow<PrettyGridBase<T, V>>()
   return with(casted){
        val newGrid = PrettyValueGrid(hostTypeToken, typeToken, opt?:options)
        newGrid.singleLoader.initFrom(singleLoader)
        newGrid.listLoader.initFrom(listLoader)
        newGrid.addRows(rows)
        newGrid
    }
}

fun <T: Any, V: Any> PrettyValueGrid<T, V>.copy(
    companion: PrettyGrid.Companion,
    opt: RowOptions? = null
):PrettyGrid<V>{
    val newGrid = PrettyGrid(typeToken, opt?:options)
    newGrid.singleLoader.initValueFrom(singleLoader)
    newGrid.listLoader.initValueFrom(listLoader)
    newGrid.addRows(rows)
    return newGrid
}
