package po.misc.data.logging.log_subject

import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.badges.Badge
import kotlin.reflect.KFunction


interface RunSubject : LogSubject{
    val context: TraceableContext
}



open class StartProcessSubject(
    override val context: TraceableContext,
    override val subjectName: String = "Start process",
) : GenericInfoSubject("", Badge.Process), RunSubject {

    fun processName(
        function: KFunction<*>,
        useBadge: Badge? = null
    ):StartProcessSubject{
        updateText("Start process ${function.name}", useBadge)
        return this
    }
}

fun Component.startProcSubject(
    processName: String,
    useBadge: Badge? = null
): StartProcessSubject {
   val subject =  StartProcessSubject(this, processName)
   subject.changeBadge(useBadge)
   return subject
}

fun Component.startProcSubject(
    function: KFunction<*>,
    text: String = "",
    useBadge: Badge? = null
): StartProcessSubject = startProcSubject("Start -> ${function.name}", useBadge)



