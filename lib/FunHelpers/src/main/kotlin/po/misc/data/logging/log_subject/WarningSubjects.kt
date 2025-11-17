package po.misc.data.logging.log_subject

import po.misc.data.badges.Badge
import po.misc.data.badges.BadgeBase
import po.misc.data.logging.NotificationTopic
import po.misc.data.styles.BGColour
import po.misc.data.styles.Emoji


interface WarningSubject: LogSubject {
    override val topic: NotificationTopic get() = NotificationTopic.Warning
    object Warning: BadgeBase(Emoji.EXCLAMATION.symbol, BGColour.Yellow), Badge
}


open class SubjectWarning(val warningText: String) : SubjectBase(Badge.Warning), WarningSubject

