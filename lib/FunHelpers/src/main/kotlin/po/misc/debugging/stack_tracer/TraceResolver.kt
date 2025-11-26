package po.misc.debugging.stack_tracer

import po.misc.context.component.Component
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.output.output
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.trace


class NotificationCondition(
    val topic: NotificationTopic,
    val code: Int
)

class TraceResolver(
    val host: Component,
    val traceResolved: (( ExceptionTrace)-> Unit)? = null
){

    internal val conditions = mutableListOf<NotificationCondition>()

    private fun checkIfResolve(topic: NotificationTopic, code: Int){

        val found = conditions.firstOrNull { it.topic == topic && it.code == code }
        if(found != null){
           val trace = host.trace()
           traceResolved?.invoke(trace)
           ExceptionTrace.callSiteReport(trace).output()
        }
    }

    fun resolveTraceWhen(topic: NotificationTopic, code: Int = 0){
        conditions.add(NotificationCondition(topic, code))
    }

    fun process(message: Loggable){
        checkIfResolve(message.topic, 0)
    }

}