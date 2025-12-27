package po.misc.data.pretty_print.parts.rows

import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.PrettyRow

data class RowParams <T>(
    val row: PrettyRow<T>,
    val usedOptions: RowOptions,
    val render: String? = null
)