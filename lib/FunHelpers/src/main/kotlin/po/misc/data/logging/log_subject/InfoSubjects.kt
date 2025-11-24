package po.misc.data.logging.log_subject

import po.misc.context.tracable.TraceableContext
import po.misc.data.badges.Badge
import po.misc.data.logging.NotificationTopic
import po.misc.debugging.ClassResolver
import kotlin.reflect.KFunction

interface InfoSubject: LogSubject{
    override val topic: NotificationTopic get() = NotificationTopic.Info
}

object Initialization : SubjectBase(Badge.Init), InfoSubject{
    fun provideContext(context: TraceableContext) : Initialization = updateText("Initializing ${ClassResolver.instanceName(context)}")
}

object MethodCall : SubjectBase(null), InfoSubject

object Configuration : SubjectBase(Badge.Config), InfoSubject

open class GenericInfoSubject(
    override var subjectText: String = "Start",
    badge: Badge = Badge.Init
) : SubjectBase(badge), InfoSubject

