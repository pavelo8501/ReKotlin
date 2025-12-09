package po.misc.debugging.stack_tracer

import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.output.output
import po.misc.debugging.classifier.HelperRecord
import po.misc.debugging.classifier.KnownHelpers
import po.misc.debugging.classifier.SimplePackageClassifier
import po.misc.debugging.stack_tracer.reports.CallSiteReport
import po.misc.debugging.stack_tracer.ExceptionTrace
import po.misc.types.k_class.simpleOrAnon
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

    private fun resolve(methodName: String, print: Boolean = true){
        val trace =  tracer.traceCallSite(methodName , hostClass.simpleOrAnon,  classifier)
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
            resolve("checkIfResolve", true)
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
        resolve("resolveTrace")
    }
    override fun resolveTrace(methodName: String){
        resolve(methodName)
    }
    override fun resolveTrace(method: KFunction<*>){
        resolve(method.name)
    }

    fun process(message: Loggable){
        checkIfResolve(message.topic, 0)
    }

}