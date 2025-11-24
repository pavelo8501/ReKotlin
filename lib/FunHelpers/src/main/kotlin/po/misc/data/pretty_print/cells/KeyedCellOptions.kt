package po.misc.data.pretty_print.cells

import po.misc.data.styles.Colour

data class KeyedCellOptions(
    val showKey: Boolean = true,
    val width: Int = 0,
    val colour: Colour? = null
)