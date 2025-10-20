package po.misc.exceptions.handling

import kotlinx.coroutines.currentCoroutineContext
import po.misc.context.tracable.TraceableContext
import po.misc.coroutines.coroutineInfo
import po.misc.data.logging.ContextAware
import po.misc.data.logging.ContextAware.ExceptionLocator
import po.misc.exceptions.trackable.TrackableException


inline fun <reified TH: Throwable> ContextAware.registerHandler(
    noinline block: (TH)-> Nothing
): Unit = ExceptionLocator.throwableRegistry.registerNoReturn<TH>(block)

inline fun <reified TH: Throwable> ContextAware.registerHandler(
    suspendable:Suspended,
    noinline block: suspend (TH)-> Nothing
): Unit = ExceptionLocator.throwableRegistry.registerNoReturn<TH>(suspendable, block)


inline fun <R: Any> TraceableContext.delegateIfThrow(block:()-> R):R{
    try {
        return block()
    }catch (throwable: Throwable){
        warn("delegateIfThrow",  throwable)
        ExceptionLocator.throwableRegistry.dispatch(throwable)
    }
}

suspend fun <R: Any> TraceableContext.delegateIfThrow(suspended:Suspended, block: suspend ()-> R):R{
    try {
        return block()
    }catch (throwable: Throwable){
        val exceptionTrace = throwable.traceFor(this::class)
        if(throwable is TrackableException){
            val context =  currentCoroutineContext()
            throwable.coroutineInfo = context.coroutineInfo(throwable.contextClass, exceptionTrace.bestPick.methodName)
        }
        warn("delegateIfThrow",  throwable)
        ExceptionLocator.throwableRegistry.dispatch(throwable, suspended)
    }
}