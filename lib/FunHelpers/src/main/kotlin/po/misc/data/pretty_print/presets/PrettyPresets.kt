package po.misc.data.pretty_print.presets

import po.misc.data.pretty_print.parts.Align
import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.CellOptions.TextStyleOptions
import po.misc.data.pretty_print.parts.KeyedCellOptions
import po.misc.data.styles.BGColour
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyle


interface StylePresets{
    val align: Align
    val style: TextStyle
    val colour: Colour?
    val backgroundColour: BGColour?
    val postfix: String?

    fun toOptions(width: Int = 0): CellOptions{
        return CellOptions(width, align, TextStyleOptions(style, colour, backgroundColour))
    }
}

interface PrettyPresets: StylePresets{

    override val align: Align
    override val style: TextStyle
    override val colour: Colour?
    override val backgroundColour: BGColour?
    override val postfix: String?

    object KotlinClass : PrettyPresets {
        override val align: Align = Align.LEFT
        override val style: TextStyle = TextStyle.Bold
        override val colour: Colour = Colour.Green
        override val backgroundColour: BGColour? = null
        override val postfix: String? = null
    }

    object Key: PrettyPresets{
        override val align: Align = Align.RIGHT
        override val style: TextStyle = TextStyle.Italic
        override val colour: Colour = Colour.Gray
        override val backgroundColour: BGColour? = null
        override val postfix: String = SpecialChars.RIGHT_SEMICOLON
    }

    object Value: PrettyPresets{
        override val align: Align = Align.LEFT
        override val style: TextStyle = TextStyle.Regular
        override val colour: Colour = Colour.CyanBright
        override val backgroundColour: BGColour? = null
        override val postfix: String? = null
    }

    object Info : PrettyPresets {
        override val align: Align = Align.LEFT
        override val style: TextStyle = TextStyle.Italic
        override val colour: Colour = Colour.GrayLight
        override val backgroundColour: BGColour? = null
        override val postfix: String? = null
    }

    object Success : PrettyPresets {
        override val align: Align = Align.LEFT
        override val style: TextStyle = TextStyle.Bold
        override val colour: Colour = Colour.GreenBright
        override val backgroundColour: BGColour? = null
        override val postfix: String? = null
    }

    object Header : PrettyPresets {
        override val align: Align = Align.CENTER
        override val style: TextStyle = TextStyle.Bold
        override val colour: Colour = Colour.BlackBright
        override val backgroundColour: BGColour = BGColour.Cyan
        override val postfix: String? = null
    }

}

interface KeyedPresets:  PrettyPresets {

    override val align: Align
    override val style: TextStyle
    override val colour: Colour?
    override val backgroundColour: BGColour?

    val keyStyle: TextStyle
    val keyColour: Colour?
    val keyBackgroundColour: BGColour?

    fun toKeyedOptions(width: Int = 0): KeyedCellOptions{
        val textStyle = styleOption()
        val keyStyle = keyStyleOption()
        return KeyedCellOptions(width, align, textStyle, keyStyle)
    }
    fun styleOption():TextStyleOptions{
        return TextStyleOptions(style, colour, backgroundColour)
    }
    fun keyStyleOption():TextStyleOptions{
       return TextStyleOptions(keyStyle, keyColour, keyBackgroundColour)
    }

    object Property: KeyedPresets{
        override val align: Align = Align.LEFT
        override val keyStyle: TextStyle = TextStyle.Italic
        override val keyColour: Colour = Colour.Magenta
        override val keyBackgroundColour: BGColour? = null

        override val style: TextStyle = TextStyle.Regular
        override val colour: Colour = Colour.GreenBright
        override val backgroundColour: BGColour? = null
        override val postfix: String? = null
    }
}





