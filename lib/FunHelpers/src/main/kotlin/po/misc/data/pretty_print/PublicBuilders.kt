package po.misc.data.pretty_print

import po.misc.callbacks.callable.asPropertyCallable
import po.misc.data.pretty_print.grid.GridBuilder
import po.misc.data.pretty_print.grid.ValueGridBuilder

import po.misc.data.pretty_print.parts.options.GridID
import po.misc.data.pretty_print.parts.options.RowID
import po.misc.data.pretty_print.rows.RowBuilder
import po.misc.data.pretty_print.rows.ValueRowBuilder
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1

inline fun <reified T: Any> buildPrettyGrid(
    gridID: GridID? = null,
    noinline builder: GridBuilder<T>.() -> Unit
): PrettyGrid<T> {
    val container = GridBuilder(
        TypeToken.create<T>(),
        gridID
    )
    builder.invoke(container)
    return container.prettyGrid
}

inline fun <reified S, reified T> prepareValueGrid(
    property: KProperty1<S, T>,
    gridID: GridID? = null,
    noinline  builderAction: ValueGridBuilder<S, T>.() -> Unit
): ValueGridBuilder<S, T> {
    val callable = property.asPropertyCallable<S, T>()
    val valueGridBuilder = ValueGridBuilder(TypeToken<S>(), TypeToken<T>(), gridID)
    valueGridBuilder.prettyGrid.dataLoader.add(callable)
    valueGridBuilder.preSaveBuilder(builderAction)
    return valueGridBuilder
}

inline fun <reified S, reified T> prepareListGrid(
    property: KProperty1<S,  List<T>>,
    gridID: GridID? = null,
    noinline  builderAction: ValueGridBuilder<S, T>.() -> Unit
): ValueGridBuilder<S, T> {
    val callable = property.asPropertyCallable<S, List<T>>()
    val valueGridBuilder = ValueGridBuilder(TypeToken<S>(), TypeToken<T>(), gridID)
    valueGridBuilder.preSaveBuilder(builderAction)
    valueGridBuilder.prettyGrid.dataLoader.add(callable)
    return valueGridBuilder
}

fun <T: Any> buildPrettyRow(
    typeToken : TypeToken<T>,
    rowId: RowID? = null,
    builder: RowBuilder<T>.()-> Unit
): PrettyRow<T> {
    val container = RowBuilder(typeToken,  rowId)
    builder.invoke(container)
    return container.finalizeRow()
}

inline fun <reified T: Any> buildPrettyRow(
    rowId: RowID? = null,
    noinline builder: RowBuilder<T>.()-> Unit
): PrettyRow<T> = buildPrettyRow(TypeToken<T>(), rowId, builder)


inline fun <reified T, reified V> prepareRow(
    property: KProperty1<T, V>,
    rowID: RowID? = null,
    noinline  builderAction: ValueRowBuilder<T, V>.() -> Unit
): ValueRowBuilder<T, V> {
    val callable = property.asPropertyCallable<T, V>()
    val valueBuilder =   ValueRowBuilder(callable.sourceType, callable.receiverType, rowID)
    valueBuilder.preSaveBuilder(builderAction)
    return valueBuilder
}

