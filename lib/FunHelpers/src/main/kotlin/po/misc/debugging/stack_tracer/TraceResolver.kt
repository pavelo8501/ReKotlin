package po.misc.debugging.stack_tracer

import po.misc.callbacks.signal.Signal
import po.misc.callbacks.signal.signalOf
import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.debugging.classifier.HelperRecord
import po.misc.debugging.classifier.KnownHelpers
import po.misc.debugging.classifier.SimplePackageClassifier
import po.misc.exceptions.stack_trace.CallSiteReport
import po.misc.exceptions.stack_trace.ExceptionTrace
import kotlin.reflect.KClass

class NotificationCondition(
    val topic: NotificationTopic,
    val code: Int
)

class TraceResolver(
    val host: Component,
    onTraceResolved: ((CallSiteReport)-> Unit)? = null
): StackTracer{

    constructor(
        host: Component,
        vararg helperRecords : HelperRecord,
        onTraceResolved: ((CallSiteReport)-> Unit)? = null
    ):this(host,  onTraceResolved){
        classifier = SimplePackageClassifier(KnownHelpers).addHelperRecords(helperRecords.toList())
    }

    private val hostClass: KClass<out  TraceableContext> =  host::class
    private var classifier = SimplePackageClassifier(KnownHelpers)

    val beforeReport: Signal<ExceptionTrace, Unit> = signalOf()
    val traceResolved:  Signal<CallSiteReport, Unit> = signalOf()

    internal val conditions = mutableListOf<NotificationCondition>()

    init {
        if(onTraceResolved != null){
            traceResolved.onSignal(onTraceResolved)
        }
    }

    private fun checkIfResolve(topic: NotificationTopic, code: Int){
        val found = conditions.firstOrNull { it.topic == topic && it.code == code }
        if(found != null){
           val trace = hostClass.createTrace(classifier)
           beforeReport.trigger(trace)
           val callSite = ExceptionTrace.callSiteReport(trace)
           traceResolved.trigger(callSite)
        }
    }


    fun resolveTraceWhen(topic: NotificationTopic, code: Int = 0){
        conditions.add(NotificationCondition(topic, code))
    }
    fun process(message: Loggable){
        checkIfResolve(message.topic, 0)
    }

}