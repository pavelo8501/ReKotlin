package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.template.RowID
import po.misc.data.pretty_print.toProvider
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


@PublishedApi
internal fun <T> createRowContainer(
    typeToken : TypeToken<T>,
    rowId: RowID? = null,
): RowBuilder<T> {
    return RowBuilder(typeToken, rowId)
}

fun <T: Any> createPrettyRow(
    typeToken : TypeToken<T>,
    commonOptions: CommonRowOptions? = null,
): PrettyRow<T> {
    val options = PrettyHelper.toRowOptions(commonOptions)
    return PrettyRow.createEmpty<T>(typeToken, options)
}


fun <T: Any> buildPrettyRow(
    typeToken : TypeToken<T>,
    rowId: RowID? = null,
    builder: RowBuilder<T>.()-> Unit
): PrettyRow<T> {
    val container = createRowContainer(typeToken,  rowId)
    builder.invoke(container)
    return container.finalizeRow()
}

inline fun <reified T, reified V> prepareRow(
    property: KProperty1<T, V>,
    rowID: RowID? = null,
    noinline  builder: ValueRowBuilder<T, V>.() -> Unit
): ValueRowBuilder<T, V> {

    val provider = property.toProvider<T, V>()
    val container = ValueRowBuilder(
        provider,
        rowID
    )
    container.preSaveBuilder(builder)
    return container
}

inline fun <reified T: Any> buildPrettyRow(
    rowId: RowID? = null,
    builder: RowBuilder<T>.()-> Unit
): PrettyRow<T> {
    val container = RowBuilder<T>(rowId)
    builder.invoke(container)
    return container.finalizeRow()
}



