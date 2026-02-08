package po.misc.data.pretty_print.parts.options

import po.misc.data.pretty_print.parts.decorator.BorderPosition
import po.misc.data.pretty_print.parts.common.BorderSet
import po.misc.data.pretty_print.parts.common.TaggedSeparator
import po.misc.data.styles.StyleCode

interface BorderPresets : BorderSet {
    override val separatorSet: List<TaggedSeparator<BorderPosition>>
    open class VerticalBorders(
        val topString: String,
        val bottomString: String,
        val style: StyleCode? = null,
    ): BorderPresets {

        constructor(topString: String, style: StyleCode? = null):this(topString, topString, style = style)
        val top: TaggedSeparator<BorderPosition> =
            TaggedSeparator(BorderPosition.Top, topString, style)
        val bottom: TaggedSeparator<BorderPosition> =
            TaggedSeparator(BorderPosition.Bottom, bottomString, style)
        override val separatorSet: List<TaggedSeparator<BorderPosition>> = buildList {
            add(top)
            add(bottom)
        }
    }

    open class HorizontalBorders(
        val leftStr: String,
        val rightStr: String,
        val style: StyleCode? = null,
    ): BorderPresets {

        constructor(leftStr: String, style: StyleCode? = null):this(leftStr, leftStr, style = style)

        val left: TaggedSeparator<BorderPosition> =
            TaggedSeparator(BorderPosition.Left, leftStr, style)

        val right: TaggedSeparator<BorderPosition> =
            TaggedSeparator(BorderPosition.Right, rightStr, style)

        override val separatorSet: List<TaggedSeparator<BorderPosition>> = buildList {
            add(left)
            add(right)
        }
    }

    object Box : BorderPresets {
        val top: TaggedSeparator<BorderPosition> get() = TaggedSeparator(BorderPosition.Top, "-")
        val bottom: TaggedSeparator<BorderPosition> get() = TaggedSeparator(BorderPosition.Bottom, "_")
        val left: TaggedSeparator<BorderPosition> get() = TaggedSeparator(BorderPosition.Left, "|")
        val right: TaggedSeparator<BorderPosition> get() = TaggedSeparator(BorderPosition.Right, "|")
        override val separatorSet: List<TaggedSeparator<BorderPosition>> get() = listOf(top, bottom, left, right)
    }

    object HalfBox : BorderPresets {
        val top: TaggedSeparator<BorderPosition> get() = TaggedSeparator(BorderPosition.Top, "-")
        val bottom: TaggedSeparator<BorderPosition> get() = TaggedSeparator(BorderPosition.Bottom, "_")
        override val separatorSet: List<TaggedSeparator<BorderPosition>> get() = listOf(top, bottom)
    }

}