package po.misc.exceptions.handling

import kotlinx.coroutines.currentCoroutineContext
import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.coroutines.coroutineInfo
import po.misc.data.logging.ContextAware
import po.misc.exceptions.ExceptionLocator
import po.misc.exceptions.TraceException
import po.misc.debugging.stack_tracer.extractTrace
import po.misc.functions.LambdaType
import po.misc.functions.Suspended


inline fun <reified TH: Throwable> ContextAware.registerHandler(
    noinline block: (TH)-> Nothing
): Unit = ExceptionLocator.throwableRegistry.registerNoReturn<TH>(block)

inline fun <reified TH: Throwable> ContextAware.registerHandler(
    suspended: Suspended,
    noinline block: suspend (TH)-> Nothing
): Unit = ExceptionLocator.throwableRegistry.registerNoReturn<TH>(suspended, block)


inline fun <R: Any> TraceableContext.delegateIfThrow(block:()-> R):R{
    try {
        return block()
    }catch (throwable: Throwable){

        ExceptionLocator.throwableRegistry.dispatch(throwable)
    }
}

suspend fun <R: Any> Component.delegateIfThrow(
    suspended: Suspended, block: suspend ()-> R
):R {
    try {
        return block()
    }catch (throwable: Throwable){
        val exceptionTrace = throwable.extractTrace()
        if(throwable is TraceException){
            val context =  currentCoroutineContext()
            throwable.coroutineInfo = context.coroutineInfo(this::class, exceptionTrace.bestPick.methodName)
        }

        warn("delegateIfThrow",  throwable)
        ExceptionLocator.throwableRegistry.dispatch(throwable, suspended)
    }
}