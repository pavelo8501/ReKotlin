package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.parts.GridKey
import po.misc.types.token.TypeToken

interface RowBuilderScope<V: Any> {
    val type: TypeToken<V>
    fun addRow(row: PrettyRow<V>): GridKey?

}