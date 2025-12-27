package po.misc.data.pretty_print.formatters.text_modifiers

import po.misc.data.NameValue
import po.misc.data.styles.TextStyler
import po.misc.types.token.TypeToken


interface NamedPriority: NameValue{
    val priority: Int get() = value
}

interface TextModifier: TextStyler{
    val formatter: NamedPriority
    val priority: Int get() = formatter.priority
    val dynamic: Boolean
    fun modify(text: String): String
}

interface ConditionalTextModifier<T>: TextModifier {
    val type: TypeToken<T>
    override val dynamic: Boolean get() = true
    fun modify(text: String, parameter: T): String?
}
