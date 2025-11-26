package po.misc.exceptions

import po.misc.context.tracable.TraceableContext
import po.misc.exceptions.handling.ThrowableRegistry
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.stack_trace.extractTrace
import po.misc.exceptions.trackable.TrackableException
import kotlin.reflect.KClass

data class HelperPackage(
    val name: String
)


abstract class ExceptionLocatorBase{

    abstract val helperPackages: MutableList<HelperPackage>
    internal  val traceMap = mutableMapOf<KClass<*>, MutableList<ExceptionTrace>>()

    @PublishedApi
    internal fun register(exceptionTrace: ExceptionTrace):ExceptionTrace{
        val resultingList = mutableListOf<ExceptionTrace>()
        if(exceptionTrace.kClass != null){
            traceMap[exceptionTrace.kClass]?.let {
                resultingList.addAll(it)
            }
            val existent =  traceMap.getOrPut(exceptionTrace.kClass){
                mutableListOf(exceptionTrace)
            }
            existent.add(exceptionTrace)
        }
        return exceptionTrace
    }

    @PublishedApi
    internal val exceptionBuilderRegistry: MutableMap<KClass<*>, (String) -> Throwable> = mutableMapOf()

    inline fun <reified TH> registerExceptionBuilder(
       noinline provider: (String)-> TH
    ) where  TH: Throwable, TH: TrackableException {
        exceptionBuilderRegistry[TH::class] = provider
    }

    val throwableRegistry: ThrowableRegistry = ThrowableRegistry()

    fun raiseManagedException(
        context: TraceableContext,
        message: String,
        traceProvider: ((ExceptionTrace)-> Unit)?
    ): ExceptionTrace {

        val managed =  ManagedException(context = context, message = message)
        val trace =  register(managed.exceptionTrace)
        traceProvider?.invoke(trace)
        throw managed
    }

    inline fun <reified TH> raiseException(
        context: TraceableContext,
        message: String,
    ): ExceptionTrace  where  TH: Throwable, TH: TrackableException {
        val exception = exceptionBuilderRegistry[TH::class]?.invoke(message) ?:run {
            ManagedException(context = context, message = message)
        }
       when(exception){
            is TrackableException ->  register(exception.exceptionTrace)
            is Throwable ->  exception.extractTrace()
        }
        throw exception
    }

    inline fun <reified TH> raiseException(
        context: TraceableContext,
        message: String,
        crossinline traceProvider: (ExceptionTrace)-> Unit
    ): ExceptionTrace  where  TH: Throwable, TH: TrackableException {

        val exception = exceptionBuilderRegistry[TH::class]?.invoke(message) ?:run {
            ManagedException(context = context, message = message)
        }

        val trace =  when(exception){
            is TrackableException ->  register(exception.exceptionTrace)
            is Throwable ->  exception.extractTrace()
        }
        traceProvider.invoke(trace)
        throw exception
    }

}