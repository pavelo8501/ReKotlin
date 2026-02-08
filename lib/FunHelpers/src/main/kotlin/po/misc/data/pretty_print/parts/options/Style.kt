package po.misc.data.pretty_print.parts.options

import po.misc.data.styles.BGColour
import po.misc.data.styles.Colour
import po.misc.data.styles.ColourStyle
import po.misc.data.styles.StyleCode
import po.misc.data.styles.TextStyle

/**
 * Visual styling options for cell content.
 *
 * @property textStyle Text style (regular, bold, italic, etc.)
 * @property colour Foreground text colour, or `null` to use default terminal colour.
 * @property backgroundColour Background fill colour, or `null` if not applied.
 */
data class Style(
    var textStyle: TextStyle = TextStyle.Regular,
    var colour: Colour = Colour.Default,
    var backgroundColour: BGColour? = null,
): StyleCode {

    override val name: String = "${textStyle.name}+${colour.name}+${backgroundColour?.name}"
    private val bgOrdinal: Int get() = backgroundColour?.ordinal?:0
    override val ordinal: Int get() = textStyle.ordinal + colour.ordinal + bgOrdinal
    override val code: String get() =
        if(backgroundColour != null){
            "${textStyle.code}${backgroundColour?.code}${colour.code}"
        }else{
            "${textStyle.code}${colour.code}"
        }

    fun copy():Style{
        return Style(textStyle, colour, backgroundColour)
    }
    override fun equals(other: Any?): Boolean {
        if(other is StyleCode){
            if(this.code == other.code) return true
        }
        return false
    }
    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + ordinal
        return result
    }
}

data class Theme(
    var textStyle: TextStyle,
    var colourStyle: ColourStyle
): StyleCode {

    override val name: String = "Theme${textStyle.name}+${colourStyle.name}"
    override val ordinal: Int get() = textStyle.ordinal + colourStyle.ordinal
    override val code: String get() = "${textStyle.code}${colourStyle.code}"
    fun copy():Theme{
        return Theme(textStyle, colourStyle)
    }
    override fun equals(other: Any?): Boolean {
        if(other is StyleCode){
            if(this.code == other.code) return true
        }
        return false
    }
    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + ordinal
        return result
    }
}