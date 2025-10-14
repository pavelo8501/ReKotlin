package po.misc.context

import po.misc.data.processors.SeverityLevel
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize


fun <T : CTX> T.whoAreYou(block: T.() -> Unit) {
    notify("Context identification by whoAreYou()", SeverityLevel.INFO)
    repeat(10) { print("-") }
    val info =
        buildString {
            appendLine()
            appendLine(detailedDump.colorize(Colour.Cyan))
        }
    println(info)
    block()
}
