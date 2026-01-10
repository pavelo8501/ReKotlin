package po.misc.data.pretty_print.formatters

import po.misc.data.NameValue

enum class FormatterTag(val priority: Int): NameValue {
    CellFormatter(1),
    TextStyler(2),
    ColorModifier(3),
    TextTrimmer(4);

    override val value: Int get() = priority
}