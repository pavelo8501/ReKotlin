package po.misc.context

import po.misc.context.models.IdentityData
import po.misc.data.processors.SeverityLevel
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import kotlin.reflect.typeOf

fun CTX.identify(
    message: String,
    block: IdentityData.() -> Unit,
) {
    block.invoke(IdentityData(this.identity.toString(), message))
}

inline fun <reified T : Any> CTX.getResolved(noinline block: T.() -> Unit) {
    (this as? T)?.let { block(it) }
}

fun <T : CTX> T.whoAreYou(block: T.() -> Unit) {
    notify("Context identification by whoAreYou()", SeverityLevel.INFO)
    repeat(10) { print("-") }
    val info =
        buildString {
            appendLine()
            appendLine(detailedDump.colorize(Colour.CYAN))
        }
    println(info)
    block()
}
