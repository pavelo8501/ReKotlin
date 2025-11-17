package po.misc.data.logging.parts

import po.misc.data.badges.Badge
import po.misc.data.badges.BadgeBase
import po.misc.data.styles.BGColour
import po.misc.data.styles.Emoji


interface LogBadge : Badge{
    override val caption: String
}
interface DebugBadge : Badge{
    override val caption: String
}

object Debug: BadgeBase("Debug"), DebugBadge

