package po.misc.data.pretty_print.formatters.text_modifiers

interface TextModifier{
    val priority: Int
    fun modify(text: String): String
}


