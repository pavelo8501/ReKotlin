package po.misc.data.pretty_print.formatters.text_modifiers


enum class Formatter(override val value: Int) : NamedPriority {
    CellFormatter(1),
    TextStyler(2),
    ColorModifier(3),
    TextTrimmer(4)

}