package po.misc.data.pretty_print

import po.misc.data.styles.Colorizer


interface CellRenderer : Colorizer {
    fun render(content: String): String
}
