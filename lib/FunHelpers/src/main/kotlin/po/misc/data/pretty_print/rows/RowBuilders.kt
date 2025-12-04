package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.parts.RowOptions
import po.misc.types.token.TypeToken

inline fun <reified T: Any> buildPrettyRow(
    rowOptions: RowOptions? = null,
    noinline builder: CellContainer<T>.()-> Unit
): PrettyRow<T> =   PrettyRow.buildRow(TypeToken.create<T>(), rowOptions, builder)


inline fun <reified T: Any> buildPrettyRow(
    container: CellContainer.Companion,
    rowOptions: RowOptions? = null,
    noinline builder: CellContainer<T>.()-> Unit
): PrettyRow<T> =  PrettyRow.buildRow(TypeToken.create<T>(), rowOptions,  builder =  builder)




