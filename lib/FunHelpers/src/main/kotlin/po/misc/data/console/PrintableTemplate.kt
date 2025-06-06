package po.misc.data.console

import po.misc.data.interfaces.Printable
import po.misc.data.interfaces.PrintableProvider

interface PrintableWithTemplate : Printable {
    fun defaultTemplate(): String = this.toString()
}

class PrintableTemplate<T: Printable>:PrintableProvider<T>{

    private val templateParts: List<T.() -> String>
    override val template: T.() -> String

    constructor(vararg template: T.() -> String, delimiter: String = "\n") {
        this.templateParts = template.toList()
        this.template = {
            templateParts.joinToString(delimiter) { it(this) }
        }
    }

    constructor(template: T.() -> String = {
        (this as? PrintableWithTemplate)?.defaultTemplate() ?: toString()
    }) {
        this.templateParts = listOf(template)
        this.template = template
    }

    internal fun resolve(receiver: T): String = template(receiver)

}


data class ConditionRule<T: Any>(
    val condition: (T) -> Boolean,
    val template: String
)

fun<T: Any> printIf(message: String,   condition: (T) -> Boolean): ConditionRule<T> =
    ConditionRule(template = message,  condition = condition)

@JvmName("printIfAttached")
fun<T: Any> String.printIf(condition: (T) -> Boolean): ConditionRule<T> =
    ConditionRule(template = this,  condition = condition)

fun <T: Any>  T.printByConditions(vararg rules: ConditionRule<T>): String {
    return rules.firstOrNull { it.condition(this) }?.template ?: ""
}