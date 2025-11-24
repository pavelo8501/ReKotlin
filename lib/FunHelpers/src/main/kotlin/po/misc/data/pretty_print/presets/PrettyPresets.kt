package po.misc.data.pretty_print.presets

import po.misc.data.pretty_print.Align
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

}

interface KeyedPresets:  PrettyPresets {

    override val align: Align
    override val style: TextStyle
    override val colour: Colour?
    override val backgroundColour: BGColour?

    val keyStyle: TextStyle
    val keyColour: Colour?
    val keyBackgroundColour: BGColour?

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





