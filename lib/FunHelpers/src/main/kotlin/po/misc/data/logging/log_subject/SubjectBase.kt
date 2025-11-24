package po.misc.data.logging.log_subject

import po.misc.data.logging.NotificationTopic
import po.misc.data.badges.Badge
import po.misc.data.logging.parts.LogBadge
import po.misc.debugging.ClassResolver
import po.misc.types.k_class.simpleOrAnon


abstract class SubjectBase(
    override var badge: Badge?
) : LogSubject {

    private var subjectBacking: String? = null
    override val subjectName: String = subjectBacking?: this::class.simpleOrAnon

    override var subjectText: String =  ClassResolver.instanceName(this)

    override fun changeText(text: String,  useBadge: Badge?): SubjectBase{
        subjectText = text
        badge = useBadge
        return this
    }

    override fun changeSubject(
        subject: String,
        text: String?,
        useBadge: Badge?
    ): SubjectBase{
        subjectBacking = subject
        text?.let {
            subjectText = it
        }
        badge = useBadge
        return this
    }

    override fun changeBadge(useBadge: Badge?): SubjectBase{
        if(useBadge != null){
            badge = useBadge
        }
        return this
    }
}

class GenericSubject(
    override var subjectText: String,
    override val topic: NotificationTopic,
    badge: LogBadge?
) : SubjectBase(badge)

fun <T: LogSubject> T.updateSubject(
    subject: String,
    text: String,
    useBadge: Badge? = null
):T {
    changeSubject(subject, text, useBadge)
    return this
}

fun <T: LogSubject> T.updateText(
    text: String,
    useBadge: Badge? = null
):T {
    changeText(text, useBadge)
    return this
}