package po.misc.debugging.stack_tracer

import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.output.output
import po.misc.debugging.classifier.HelperRecord
import po.misc.debugging.classifier.KnownHelpers
import po.misc.debugging.classifier.SimplePackageClassifier
import po.misc.exceptions.stack_trace.CallSiteReport
import po.misc.exceptions.stack_trace.ExceptionTrace
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

class NotificationCondition(
    val topic: NotificationTopic,
    val code: Int
)

interface StackResolver{
    fun resolveTrace()
    fun resolveTrace(methodName: String)
    fun resolveTrace(method: KFunction<*>)
}

class TraceResolver(
    val host: Component,
    onTraceResolved: ((CallSiteReport)-> Unit)? = null
): StackResolver{

    constructor(
        host: Component,
        vararg helperRecords : HelperRecord,
        onTraceResolved: ((CallSiteReport)-> Unit)? = null
    ):this(host,  onTraceResolved){
        classifier = SimplePackageClassifier(KnownHelpers).addHelperRecords(helperRecords.toList())
    }

    private val tracer = StackTracerClass()

    private val hostClass: KClass<out  TraceableContext> =  host::class
    private var classifier = SimplePackageClassifier(KnownHelpers)

    private var beforeReportCallback : ((ExceptionTrace) -> Unit)? = null
    private var traceResolvedCallback : ((CallSiteReport) -> Unit)? = null

    internal val conditions = mutableListOf<NotificationCondition>()
    internal val methodConditions = mutableListOf<KFunction<*>>()

    init {
        if(onTraceResolved != null){
            traceResolvedCallback = onTraceResolved
        }
    }

    private fun resolve(print: Boolean,  methodName: String?){
        val trace =  tracer.traceCallSite(hostClass, methodName, classifier)
        beforeReportCallback?.invoke(trace)
        val callSite = ExceptionTrace.callSiteReport(trace)
        if(print){
            callSite.output()
        }
        traceResolvedCallback?.invoke(callSite)
    }

    private fun checkIfResolve(topic: NotificationTopic, code: Int){
        val found = conditions.firstOrNull { it.topic == topic && it.code == code }
        if(found != null){
           val trace =tracer.traceCallSite(hostClass, null, classifier)
            beforeReportCallback?.invoke(trace)
            val callSite = ExceptionTrace.callSiteReport(trace)
            traceResolvedCallback?.invoke(callSite)
        }
    }

    fun beforeReport(callback: (ExceptionTrace) -> Unit){
        beforeReportCallback = callback
    }

    fun traceResolved(callback: (CallSiteReport) -> Unit){
        traceResolvedCallback = callback
    }

    fun resolveTraceWhen(topic: NotificationTopic, code: Int = 0){
        conditions.add(NotificationCondition(topic, code))
    }
    fun resolveTraceWhen(method: KFunction<*>){
        methodConditions.add(method)
    }

    override fun resolveTrace(){
        resolve(true, null)
    }
    override fun resolveTrace(methodName: String){
        resolve(true,  methodName)
    }
    override fun resolveTrace(method: KFunction<*>){
        resolve(true,  method.name)
    }

    fun process(message: Loggable){
        checkIfResolve(message.topic, 0)
    }

}