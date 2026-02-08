package po.misc.data.pretty_print.parts.common

import po.misc.data.styles.StyleCode

interface PrettyPrintAuxBuilder{

    fun <E: Enum<E>> String.toSeparator(tag: E, styleCode: StyleCode? = null): TaggedSeparator<E> =
        TaggedSeparator(tag, this, styleCode, 1)

    fun <E: Enum<E>> String.toSeparator(tag: E, repeat: Int,  styleCode: StyleCode? = null): TaggedSeparator<E> =
        TaggedSeparator(tag, this, styleCode, repeat)

    fun String.toSeparator(styleCode: StyleCode?, repeat: Int = 1): Separator = Separator(this, styleCode, repeat)

}