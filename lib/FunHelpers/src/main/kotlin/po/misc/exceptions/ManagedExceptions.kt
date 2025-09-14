package po.misc.exceptions

import po.misc.data.printable.knowntypes.PropertyData
import po.misc.context.CTX
import po.misc.exceptions.models.ExceptionData
import po.misc.exceptions.models.ExceptionTrace

enum class HandlerType(val value: Int) {
    SkipSelf(1),
    CancelAll(2);
    companion object {
        fun fromValue(value: Int): HandlerType {
            return entries.firstOrNull { it.value == value } ?:CancelAll
        }
    }
}


interface TrackableException {
    val exceptionTrace: ExceptionTrace
}



/**
 * A managed application exception that carries:
 * - [msg]   → Human-readable description
 * - [code]  → Optional error code enum
 * - [context] → Arbitrary caller context (temporary: currently accepts [Any])
 *
 * ## Migration Note
 * - For now [context] accepts [Any], but in the `init` block we enforce that
 *   it implements [TraceableContext].
 * - This allows gradual transition: existing call sites compile,
 *   but runtime validation ensures correct usage.
 * - Later the constructor signature can be narrowed to `TraceableContext`
 *   once all callers are migrated.
 *
 * Example:
 * ```
 * class MyContext : TraceableContext
 * throw ManagedException("Something failed", MyErrorCode.FATAL, MyContext())
 * ```
 *
 * @see TraceableContext Marker interface for objects that can be used as exception context
 */
open class ManagedException(
    open val context: Any,
    override val message: String,
    open val code: Enum<*>? = null,
    override val  cause : Throwable? = null
) : Throwable(message, cause), TrackableException{

    enum class ExceptionEvent{
        Thrown,
        HandlerChanged,
        Rethrown,
        Executed
    }


    private var payloadBacking: ManagedCallSitePayload? = null
    val exceptionData: MutableList<ExceptionData> =  mutableListOf()
    internal val payload:  ManagedCallSitePayload? get() = payloadBacking
    val methodName: String? get() = payloadBacking?.methodName
   // open val context: CTX? get() = payloadBacking?.context

    open var handler: HandlerType = HandlerType.CancelAll
        internal set

    override val exceptionTrace: ExceptionTrace = createMeta(context)

    init {
//        val original = super.fillInStackTrace().stackTrace
//        val injected = trace?.stackFrames?.map {
//            StackTraceElement(
//                it.simpleClassName,
//                trace.injectedAsAny as String,
//                it.fileName,
//                it.lineNumber,
//            )
//        } ?: emptyList()
//        stackTrace = (injected + original).toTypedArray()

    }

    constructor(managedPayload: ManagedCallSitePayload):
            this(
                managedPayload.context,
                managedPayload.message,
                managedPayload.code,
                managedPayload.cause
            ) {
        createMeta(managedPayload.context)
        initFromPayload(managedPayload)
    }

    private fun createMeta(callingContext: Any):  ExceptionTrace{
       return when(callingContext){
            is TraceableContext -> {
                metaFrameTrace(callingContext::class)
            }
            else -> {
                metaFrameTrace(callingContext::class)
            }
        }
    }

    protected fun initFromPayload(payload: ManagedCallSitePayload){
        payloadBacking = payload

        val stackFrameMeta = extractCallSiteMeta(payload.methodName, framesCount = 3)
        val data = ExceptionData(ExceptionEvent.Thrown, payload.message, null)
        data.addStackTraceMeta(stackFrameMeta)
        data.addStackTrace(currentCallerTrace(payload.methodName))
        exceptionData.add(data)
    }

    fun addExceptionData(data: ExceptionData, context: CTX):ManagedException{
        val data = ExceptionData(ExceptionEvent.Thrown, message, context)
        exceptionData.add(data)
        return this
    }

    fun setHandler(handlerType: HandlerType, producer: CTX): ManagedException {
        if (exceptionData.isEmpty()) {
            val data = ExceptionData(ExceptionEvent.Thrown, message, producer)
            data.addStackTrace(stackTrace.toList())
            exceptionData.add(data)
        } else {
            val data =
                ExceptionData(ExceptionEvent.HandlerChanged, message, producer)
            exceptionData.add(data)
        }
        if (handler != handlerType) {
            handler = handlerType
        }
        return this
    }

    fun throwSelf(methodName: String, producer: CTX, event: ExceptionEvent = ExceptionEvent.Thrown):Nothing {
        val data = ExceptionData(event, message, producer)
        data.addStackTrace(currentCallerTrace(methodName))
        exceptionData.add(data)
        throw this
    }

    fun setPropertySnapshot(snapshot: List<PropertyData>?):ManagedException{
        if(snapshot != null){
            val lastData = exceptionData.lastOrNull()
            lastData?.addPropertySnapshot(snapshot)
        }
        return this
    }
}


