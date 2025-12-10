package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.PrettyRow
import po.misc.types.token.TypeToken

fun <T: Any, V: Any> PrettyRow<T>.copyRow(token: TypeToken<V>): PrettyRow<V> {
    return PrettyRow(token, options, cells)
}