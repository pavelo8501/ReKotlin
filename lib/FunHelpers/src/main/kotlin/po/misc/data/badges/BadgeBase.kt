package po.misc.data.badges

import po.misc.data.logging.parts.LogBadge
import po.misc.data.styles.BGColour
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize


abstract class BadgeBase(
    val rawText: String,
    val background: BGColour = BGColour.White,
    val  foreground: Colour = Colour.WhiteBright
) : Badge {

    var unformatted: String  = "[ ${rawText.uppercase()} ]"
    override val caption: String = unformatted.colorize(background, foreground)
    override fun toString(): String = caption
}

class GenericBadge(
    rawText: String,
    foreground: Colour,
    background: BGColour
): BadgeBase(rawText, background, foreground), LogBadge{

    constructor(rawText: String):this(rawText, Colour.WhiteBright, BGColour.Yellow)

}