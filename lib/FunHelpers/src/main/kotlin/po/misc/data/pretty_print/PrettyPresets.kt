package po.misc.data.pretty_print

import po.misc.data.styles.BGColour
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyle


interface PrettyPresets{
    val align: Align
    val style: TextStyle?
    val colour: Colour?
    val background: BGColour?
    val postfix: String get() = ""


    object KClass : PrettyPresets {
        override val align: Align = Align.LEFT
        override val style: TextStyle = TextStyle.BOLD
        override val colour: Colour = Colour.Green
        override val background: BGColour? = null
    }


    object Key: PrettyPresets{
        override val align: Align = Align.RIGHT
        override val style: TextStyle = TextStyle.ITALIC
        override val colour: Colour = Colour.Gray
        override val background: BGColour? = null
        override val postfix: String = SpecialChars.RIGHT_SEMICOLON
    }


    object Value: PrettyPresets{
        override val align: Align = Align.LEFT
        override val style: TextStyle? = null
        override val colour: Colour = Colour.CyanBright
        override val background: BGColour? = null
    }

    object Info : PrettyPresets {
        override val align: Align = Align.LEFT
        override val style: TextStyle = TextStyle.ITALIC
        override val colour: Colour = Colour.GrayLight
        override val background: BGColour? = null
    }

    object Success : PrettyPresets {
        override val align: Align = Align.LEFT
        override val style: TextStyle = TextStyle.BOLD
        override val colour: Colour = Colour.GreenBright
        override val background: BGColour? = null
    }


}
