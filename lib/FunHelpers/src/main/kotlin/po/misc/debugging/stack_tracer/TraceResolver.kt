package po.misc.debugging.stack_tracer

import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.Loggable
import po.misc.data.logging.Topic
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
    val topic: Topic,
    val code: Int
)

interface StackResolver{
    fun resolveTrace()
    fun resolveTrace(methodName: String)
    fun resolveTrace(method: KFunction<*>)
}

class TraceResolver(
    val host: Component,
    val records: List<HelperRecord> = listOf(),
    onTraceResolved: ((CallSiteReport)-> Unit)? = null
): StackResolver {

    constructor(
        host: Component,
        vararg helperRecords: HelperRecord,
        onTraceResolved: ((CallSiteReport) -> Unit)? = null
    ) : this(host, helperRecords.toList(), onTraceResolved)


    private val hostClass: KClass<out TraceableContext> = host::class

    internal val classifier = SimplePackageClassifier(KnownHelpers.classRecords + records)
    internal val stackTracer = StackTracerClass(classifier)

    private var beforeCallback: ((ExceptionTrace) -> Unit)? = null
    private var resolvedCallback: ((CallSiteReport) -> Unit)? = null

    internal val conditions = mutableListOf<NotificationCondition>()
    internal val methodConditions = mutableListOf<KFunction<*>>()

    init {
        if (onTraceResolved != null) {
            resolvedCallback = onTraceResolved
        }
    }

    private fun resolve(methodName: String, print: Boolean = true) {
        val trace = stackTracer.traceCallSite(methodName, hostClass.simpleOrAnon)
        beforeCallback?.invoke(trace)
        val callSite = trace.callSite()
        if (print) {
            callSite.output()
        }
        resolvedCallback?.invoke(callSite)
    }

    private fun checkIfResolve(topic: Topic, code: Int) {
        val found = conditions.firstOrNull { it.topic == topic && it.code == code }
        if (found != null) {
            resolve("checkIfResolve", true)
        }
    }

    private fun shouldResolve(topic: Topic): Boolean {
        return conditions.any { it.topic == topic }
    }

    fun beforeReport(callback: (ExceptionTrace) -> Unit) {
        beforeCallback = callback
    }

    fun traceResolved(callback: (CallSiteReport) -> Unit) {
        resolvedCallback = callback
    }

    fun resolveTraceWhen(topic: Topic, code: Int = 0) {
        conditions.add(NotificationCondition(topic, code))
    }

    fun resolveTraceWhen(method: KFunction<*>) {
        methodConditions.add(method)
    }

    override fun resolveTrace() {
        resolve("resolveTrace")
    }

    override fun resolveTrace(methodName: String) {
        resolve(methodName)
    }

    override fun resolveTrace(method: KFunction<*>) {
        resolve(method.name)
    }

    fun processMsg(message: Loggable, print: Boolean = true): CallSiteReport? {
        if (shouldResolve(message.topic)) {
            val trace = stackTracer.traceCallSite("processMsg")
            beforeCallback?.invoke(trace)
            val callSite = trace.callSite()
            if (print) {
                callSite.output()
            }
            resolvedCallback?.invoke(callSite)
            return callSite
        }
        return null
    }
}