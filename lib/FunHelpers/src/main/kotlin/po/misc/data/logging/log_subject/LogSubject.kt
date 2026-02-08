package po.misc.data.logging.log_subject

import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.Topic
import po.misc.data.badges.Badge
import po.misc.data.badges.BadgeBase
import po.misc.data.logging.parts.LogBadge
import po.misc.data.styles.BGColour
import po.misc.data.styles.Emoji
import po.misc.debugging.ClassResolver
import po.misc.interfaces.named.TextContaining
import kotlin.reflect.KFunction


sealed interface LogSubject : TextContaining {

    val subjectText: String
    val badge: Badge?
    val subjectName: String get() =  ClassResolver.instanceName(this)
    val topic: Topic

    override fun asText(): String = "${badge?.caption.orEmpty()} $subjectText"

    fun changeBadge(useBadge: Badge?): LogSubject
    fun changeText(text: String, useBadge: Badge? = null): LogSubject
    fun changeSubject(subject: String, text: String? = null, useBadge: Badge? = null): SubjectBase

    companion object {
        fun subject(text: String, topic: Topic, badge: LogBadge? = null): GenericSubject {
           return GenericSubject(text, topic,  badge)
        }
    }
}

interface ExceptionSubject: LogSubject{
    override val topic: Topic get() = Topic.Exception
}

interface DebugSubject: LogSubject{
    override val topic: Topic get() = Topic.Debug
    object Debug: BadgeBase(Emoji.HAMMER.symbol, BGColour.White), Badge
}

object SubjectInit : SubjectBase(Badge.Init), InfoSubject{
    fun provideContext(
        context: TraceableContext
    ) : SubjectInit = updateText("Initializing ${ClassResolver.instanceName(context)}")
}
















