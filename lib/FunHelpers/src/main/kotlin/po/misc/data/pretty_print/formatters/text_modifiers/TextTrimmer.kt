package po.misc.data.pretty_print.formatters.text_modifiers


open class TextTrimmer(val maxLength: Int, val applyText: String): TextModifier {
    override val priority: Int = 0
    override fun modify(text: String): String {
        return text.take(maxLength.coerceAtMost(text.length)) + applyText
    }
}
