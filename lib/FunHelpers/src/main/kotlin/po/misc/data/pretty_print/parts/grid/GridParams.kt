package po.misc.data.pretty_print.parts.grid

import po.misc.data.pretty_print.PrettyGridBase
import po.misc.data.pretty_print.parts.RowOptions

data class GridParams<T: Any, V: Any>(
    val row: PrettyGridBase<T, V>,
    val usedOptions: RowOptions
)