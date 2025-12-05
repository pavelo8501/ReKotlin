package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.grid.PrettyGrid
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.types.token.TypeToken


inline fun <reified T: Templated> T.buildPrettyGrid(builder: PrettyGrid<T>.() -> Unit):PrettyGrid<T>{
    val token = TypeToken.create<T>()
    val grid = PrettyGrid<T>(token)
    builder.invoke(grid)
    return grid
}

inline fun <reified T: Templated> T.buildPrettyRow(
    rowOptions: RowOptions? = null,
    builder: CellReceiverContainer<T, T>.(T)-> Unit
): PrettyRow<T> {
    val options = PrettyHelper.toRowOptionsOrDefault(rowOptions)
    val token = TypeToken.create<T>()
    val container = CellReceiverContainer<T, T>(token, token,  options)
    val realRow = PrettyRow<T>(container)
    return realRow
}