package po.misc.data.pretty_print.parts.options

import po.misc.data.styles.BGColour
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyle

/**
 * Predefined styling presets for commonly used cell types.
 *
 * Presets provide convenient access to reusable combinations of
 * alignment, text style, colours, background colours, and postfixes.
 *
 * Call [asOptions] to convert a preset into a configurable [Options] instance.
 */
interface CellPresets: CommonCellOptions {

    val align: Align
    override val style: Style
    override val keyStyle: Style
    override val plainKey: Boolean get() = false
    val renderKey : Boolean
    val postfix: String?

    override fun asOptions(width: Int): Options {
        return Options(width, align, style, keyStyle = keyStyle, renderKey = renderKey)
    }

    object KotlinClass : CellPresets {
        override val align: Align = Align.LEFT
        override val style: Style get() = Style(TextStyle.Bold, Colour.Green)
        override val keyStyle: Style = Style()
        override val renderKey : Boolean = false
        override val postfix: String? = null
    }

    object Key: CellPresets{
        override val align: Align = Align.RIGHT
        override val style: Style get() = Style(TextStyle.Italic, Colour.Gray)
        override val keyStyle: Style = Style()
        override val renderKey : Boolean = false
        override val postfix: String = SpecialChars.RIGHT_SEMICOLON
    }

    object Value: CellPresets{
        override val align: Align = Align.LEFT
        override val style: Style get() = Style(TextStyle.Regular, Colour.CyanBright)
        override val keyStyle: Style = Style()
        override val renderKey : Boolean = false
        override val postfix: String? = null
    }

    object Info : CellPresets {
        override val align: Align = Align.LEFT
        override val style: Style get() = Style(TextStyle.Italic, Colour.GrayLight)
        override val keyStyle: Style = Style()
        override val renderKey : Boolean = false
        override val postfix: String? = null
    }

    object Success : CellPresets {
        override val align: Align = Align.LEFT
        override val style: Style get() = Style(TextStyle.Bold, Colour.GreenBright)
        override val keyStyle: Style = Style()
        override val renderKey : Boolean = false
        override val postfix: String? = null
    }

    object Header : CellPresets {
        override val align: Align = Align.CENTER
        override val style: Style get() = Style(TextStyle.Bold, Colour.BlackBright, BGColour.Cyan)
        override val keyStyle: Style = Style()
        override val renderKey : Boolean = false
        override val postfix: String? = null
    }

    object PlainText: CellPresets {
        override val align: Align = Align.LEFT
        override val style: Style get() = Style()
        override val keyStyle: Style = Style()
        override val renderKey : Boolean = false
        override val postfix: String? = null
    }

    object Property: CellPresets{
        override val align: Align = Align.LEFT
        override val keyStyle: Style get() = Style(TextStyle.Italic, Colour.Magenta)
        override val style: Style get() = Style(TextStyle.Regular, Colour.GreenBright)
        override val postfix: String? = null
        override val renderKey : Boolean = true
    }

    object KeylessProperty: CellPresets{
        override val align: Align = Align.LEFT
        override val keyStyle: Style get() = Style()
        override val style: Style get() = Style(TextStyle.Regular, Colour.Green)
        override val postfix: String? = null
        override val renderKey : Boolean = false
    }

}