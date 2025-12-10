package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1



@PublishedApi
internal fun <T: Any> createRowContainer(
    typeToken : TypeToken<T>,
    commonOptions: CommonRowOptions? = null,
): RowContainer<T> {
    val options = PrettyHelper.toRowOptions(commonOptions)
    return RowContainer(typeToken,  options =  options)
}

internal fun <T: Any, V: Any> createRowValueContainer(
    hostTypeToken : TypeToken<T>,
    typeToken : TypeToken<V>,
    commonOptions: CommonRowOptions? = null,
): RowValueContainer<T, V> {
    val options = PrettyHelper.toRowOptions(commonOptions)
    return RowValueContainer(hostTypeToken, typeToken,   options =  options)
}

fun <T: Any> createPrettyRow(
    typeToken : TypeToken<T>,
    commonOptions: CommonRowOptions? = null,
): PrettyRow<T> {
    val options = PrettyHelper.toRowOptions(commonOptions)
    return PrettyRow<T>(typeToken, options)
}


inline fun <reified T: Any> createPrettyRow(
    commonOptions: CommonRowOptions? = null,
): PrettyRow<T> = createPrettyRow(TypeToken.create<T>(),  commonOptions)


fun <T: Any> buildPrettyRow(
    typeToken : TypeToken<T>,
    commonOptions: CommonRowOptions? = null,
    builder: RowContainer<T>.()-> Unit
): PrettyRow<T> {
    val options = PrettyHelper.toRowOptions(commonOptions)
    val container = createRowContainer(typeToken, options)
    return container.applyBuilder(builder)
}

inline fun <reified T: Any> buildPrettyRow(
    commonOptions: CommonRowOptions? = null,
    noinline builder: RowContainer<T>.()-> Unit
): PrettyRow<T> = buildPrettyRow(TypeToken.create<T>(), commonOptions, builder)

inline fun <reified T: Any> buildPrettyRow(
    rowId: Enum<*>,
    orientation: Orientation? = null,
    noinline builder: RowContainer<T>.()-> Unit
): PrettyRow<T> = buildPrettyRow(TypeToken.create<T>(), RowOptions(rowId, orientation), builder)



inline fun <reified T: Templated> T.buildRowForContext(
    rowOptions: CommonRowOptions? = null,
    noinline builder: RowContainer<T>.()-> Unit
): PrettyRow<T> {
    val container = RowContainer<T>(TypeToken.create<T>(), PrettyHelper.toRowOptions(rowOptions))
    return container.applyBuilder(builder)
}


fun <T: Any, V: Any> buildPrettyRow(
    property: KProperty1<T, V>,
    typeToken: TypeToken<T>,
    valueToken: TypeToken<V>,
    rowOptions: CommonRowOptions? = null,
    builder: RowValueContainer<T, V>.()-> Unit
): PrettyRow<V> {
    val options = PrettyHelper.toRowOptions(rowOptions)
    val rowContainer = createRowValueContainer(typeToken, valueToken, options)
    rowContainer.singleLoader.setProperty(property)
    return rowContainer.applyBuilder(builder)
}


inline fun <T: Any, reified V: Any> buildPrettyRow(
    typeToken: TypeToken<T>,
    property: KProperty1<T, V>,
    rowOptions: CommonRowOptions? = null,
    noinline builder: RowValueContainer<T, V>.()-> Unit
): PrettyRow<V> = buildPrettyRow(property, typeToken, TypeToken.create<V>(), rowOptions, builder)

inline fun <reified T: Any, reified V: Any> buildPrettyRow(
    property: KProperty1<T, V>,
    rowOptions: CommonRowOptions? = null,
    noinline builder: RowValueContainer<T, V>.()-> Unit
): PrettyRow<V> = buildPrettyRow(property, TypeToken.create<T>(), TypeToken.create<V>(), rowOptions, builder)






