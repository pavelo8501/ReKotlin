package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.grid.PrettyGrid
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.types.token.TypeToken


inline fun <reified T: Templated> T.buildPrettyGrid(builder: PrettyGrid<T>.() -> Unit):PrettyGrid<T>{
    val token = TypeToken.create<T>()
    val grid = PrettyGrid<T>(token)
    builder.invoke(grid)
    return grid
}

inline fun <reified T: Templated> T.buildPrettyRow(builder: CellReceiverContainer<T>.(T)-> Unit): PrettyRow<T> {
    val constructor = CellReceiverContainer<T>(this, TypeToken.create(), RowOptions())
    builder.invoke(constructor, this)
    val realRow = PrettyRow<T>(constructor)
    return realRow
}

inline fun <reified T: Templated> T.buildPrettyRow(
    rowOptions: RowOptions? = null,
    noinline builder: CellReceiverContainer<T>.(T)-> Unit
): PrettyRow<T> =  PrettyRow.buildRowForContext(this, TypeToken.create<T>(), rowOptions,  builder)


fun <T: Templated> T.buildPrettyRow(
    typeToken: TypeToken<T>,
    rowOptions: RowOptions? = null,
    builder: CellReceiverContainer<T>.(T)-> Unit
): PrettyRow<T> =  PrettyRow.buildRowForContext(this, typeToken, rowOptions,  builder)