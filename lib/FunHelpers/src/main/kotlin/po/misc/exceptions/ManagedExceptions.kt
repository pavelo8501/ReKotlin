package po.misc.exceptions

import po.misc.data.printable.knowntypes.PropertyData
import po.misc.context.CTX
import po.misc.context.tracable.TraceableContext
import po.misc.coroutines.CoroutineInfo
import po.misc.exceptions.models.ExceptionData
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.stack_trace.extractTrace
import po.misc.exceptions.trackable.TrackableException
import kotlin.reflect.KClass

enum class HandlerType(val value: Int) {
    SkipSelf(1),
    CancelAll(2);
    companion object {
        fun fromValue(value: Int): HandlerType {
            return entries.firstOrNull { it.value == value } ?:CancelAll
        }
    }
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
    val context: Any,
    override val message: String,
    open val code: Enum<*>? = null,
    override val  cause : Throwable? = null
) : Throwable(message, cause), TrackableException {

    enum class ExceptionEvent{
        Thrown,
        HandlerChanged,
        Rethrown,
        Executed
    }

    override val contextClass: KClass<*> = context::class
    override var coroutineInfo: CoroutineInfo? = null

    override val self: Throwable
        get() = this


    private var payloadBacking: ThrowableCallSitePayload? = null
    val exceptionData: MutableList<ExceptionData> =  mutableListOf()
    internal val payload:  ThrowableCallSitePayload? get() = payloadBacking
    val methodName: String? get() = payloadBacking?.methodName

    open var handler: HandlerType = HandlerType.CancelAll
        internal set

    override var exceptionTrace: ExceptionTrace = extractTrace(context)


    constructor(managedPayload: ThrowableCallSitePayload):
            this(
                managedPayload.context,
                managedPayload.message,
                managedPayload.code,
                managedPayload.cause
            ) {
        initFromPayload(managedPayload)
    }

    protected fun initFromPayload(managedPayload: ThrowableCallSitePayload){
        exceptionTrace = extractTrace(managedPayload)
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

    fun setPropertySnapshot(snapshot: List<PropertyData>?):ManagedException{
        if(snapshot != null){
            val lastData = exceptionData.lastOrNull()
            lastData?.addPropertySnapshot(snapshot)
        }
        return this
    }
}


fun <T:TraceableContext> T.managedException(message: String): ManagedException{
  return  ManagedException(this, message, null)
}

fun <T:TraceableContext> T.managedException(message: String, code: Enum<*>): ManagedException{
    return  ManagedException(this, message, code)
}

fun <T:TraceableContext> T.managedException(message: String, code: Enum<*>?, cause: Throwable?): ManagedException{
    return  ManagedException(this, message, code, cause)
}



