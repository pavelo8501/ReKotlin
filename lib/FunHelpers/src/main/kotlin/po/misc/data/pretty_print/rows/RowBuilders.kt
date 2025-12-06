package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.grid.PrettyValueGrid
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


fun <T: Any> buildPrettyRow(
    typeToken : TypeToken<T>,
    commonOptions: CommonRowOptions? = null,
    builder: CellContainer<T>.()-> Unit
): PrettyRow<T> {
    val options = PrettyHelper.toRowOptionsOrDefault(commonOptions)
    val container = CellContainer(typeToken, options)
    return  container.buildRow(builder)
}

inline fun <reified T: Any> buildPrettyRow(
    commonOptions: CommonRowOptions? = null,
    noinline builder: CellContainer<T>.()-> Unit
): PrettyRow<T> = buildPrettyRow(TypeToken.create<T>(), commonOptions, builder)



fun <T: Any, V: Any> buildPrettyRow(
    property: KProperty1<T, V>,
    typeToken: TypeToken<T>,
    valueToken: TypeToken<V>,
    rowOptions: CommonRowOptions? = null,
    builder: CellReceiverContainer<T, V>.()-> Unit
):  PrettyRow<V>{
    val options = PrettyHelper.toRowOptionsOrDefault(rowOptions)
    val receiverContainer = CellReceiverContainer(typeToken, valueToken, options)
    builder.invoke(receiverContainer)
    val grid =  PrettyValueGrid(typeToken, valueToken, options)
    grid.singleLoader.setReadOnlyProperty(property)
    grid.addRow( receiverContainer.prettyRow)
    return receiverContainer.prettyRow
}

inline fun <reified T: Any, reified V: Any> buildPrettyRow(
    property: KProperty1<T, V>,
    rowOptions: CommonRowOptions? = null,
    noinline builder: CellReceiverContainer<T, V>.()-> Unit
):  PrettyRow<V> = buildPrettyRow(property, TypeToken.create<T>(), TypeToken.create<V>(), rowOptions, builder)






