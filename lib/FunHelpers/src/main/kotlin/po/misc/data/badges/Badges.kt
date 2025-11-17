package po.misc.data.badges

import po.misc.data.TextContaining
import po.misc.data.logging.parts.DebugBadge
import po.misc.data.logging.parts.LogBadge
import po.misc.data.styles.BGColour
import po.misc.data.styles.Colour
import po.misc.data.styles.Emoji

interface Badge {
    val caption: String

    object Synchronization: BadgeBase("sync"), LogBadge
    object Config: BadgeBase("conf"), LogBadge
    object Process: BadgeBase("proc"), LogBadge
    object Init: BadgeBase("init"), LogBadge
    object Parse: BadgeBase("parse"), LogBadge

    object Warning: BadgeBase(Emoji.EXCLAMATION.symbol, BGColour.Yellow), Badge


    companion object{
        fun make(text: String): GenericBadge {
            return GenericBadge(text)
        }
        fun make(text: String, foreground: Colour, background: BGColour): GenericBadge {
            return GenericBadge(text, foreground, background)
        }
        fun make(textContaining: TextContaining, foreground: Colour, background: BGColour): GenericBadge {
            return GenericBadge(textContaining.asText(), foreground, background)
        }
    }
}

