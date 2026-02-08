package po.misc.data.text_span

import po.misc.data.Separator

fun List<OrderedText>.mergeTexts(separator: Separator = Separator()):OrderedText{
    val text = OrderedText()
    forEach {
        text.merge(it, separator)
    }
    return text
}