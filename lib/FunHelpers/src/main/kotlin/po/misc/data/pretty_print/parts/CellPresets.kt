package po.misc.data.pretty_print.parts

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
 * Call [toOptions] to convert a preset into a configurable [Options] instance.
 */
interface CellPresets: CommonCellOptions{

    val align: Align
    val postfix: String?


    fun toOptions(width: Int = 0): Options{
        return Options(width, align,  styleOptions)
    }

    override fun asOptions(): Options = toOptions()

    object KotlinClass : CellPresets {
        override val align: Align = Align.LEFT
        override val styleOptions: TextStyleOptions get() = TextStyleOptions(TextStyle.Bold, Colour.Green)
        override val postfix: String? = null
    }

    object Key: CellPresets{
        override val align: Align = Align.RIGHT
        override val styleOptions: TextStyleOptions get() = TextStyleOptions(TextStyle.Italic, Colour.Gray)
        override val postfix: String = SpecialChars.RIGHT_SEMICOLON
    }

    object Value: CellPresets{
        override val align: Align = Align.LEFT
        override val styleOptions: TextStyleOptions get() = TextStyleOptions(TextStyle.Regular, Colour.CyanBright)
        override val postfix: String? = null
    }

    object Info : CellPresets {
        override val align: Align = Align.LEFT
        override val styleOptions: TextStyleOptions get() = TextStyleOptions(TextStyle.Italic, Colour.GrayLight)
        override val postfix: String? = null
    }

    object Success : CellPresets {
        override val align: Align = Align.LEFT
        override val styleOptions: TextStyleOptions get() = TextStyleOptions(TextStyle.Bold, Colour.GreenBright)
        override val postfix: String? = null
    }

    object Header : CellPresets {
        override val align: Align = Align.CENTER
        override val styleOptions: TextStyleOptions get() = TextStyleOptions(TextStyle.Bold, Colour.BlackBright, BGColour.Cyan)
        override val postfix: String? = null
    }
}

interface KeyedPresets : CommonCellOptions{

    val align: Align
    val postfix: String?
    val keyStyleOptions : TextStyleOptions

    fun toOptions(width: Int = 0): KeyedOptions{
       return KeyedOptions(this)
    }

    override fun asOptions(): Options{
        return Options(0, align,  styleOptions)
    }
    object Property: KeyedPresets{
        override val align: Align = Align.LEFT
        override val keyStyleOptions: TextStyleOptions get() =  TextStyleOptions(TextStyle.Italic, Colour.Magenta)
        override val styleOptions: TextStyleOptions get() =  TextStyleOptions(TextStyle.Regular, Colour.GreenBright)
        override val postfix: String? = null
    }
}





