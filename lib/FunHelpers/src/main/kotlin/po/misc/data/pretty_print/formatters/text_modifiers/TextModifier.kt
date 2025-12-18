package po.misc.data.pretty_print.formatters.text_modifiers

import po.misc.data.NameValue
import po.misc.types.token.TypeToken


interface NamedPriority: NameValue{
    val priority: Int get() = value
}

interface TextModifier{
    val formatter: NamedPriority
    val priority: Int get() = formatter.priority
    fun modify(text: String): String
}

interface ConditionalTextModifier<T>: TextModifier {
    val type: TypeToken<T>
    fun modify(text: String, parameter: T): String?
}
