package po.misc.exceptions.handling

import kotlinx.coroutines.currentCoroutineContext
import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.coroutines.coroutineInfo
import po.misc.data.logging.ContextAware
import po.misc.exceptions.ExceptionLocator
import po.misc.exceptions.stack_trace.extractTrace
import po.misc.exceptions.trackable.TrackableException
import po.misc.functions.LambdaType


inline fun <reified TH: Throwable> ContextAware.registerHandler(
    noinline block: (TH)-> Nothing
): Unit = ExceptionLocator.throwableRegistry.registerNoReturn<TH>(block)

inline fun <reified TH: Throwable> ContextAware.registerHandler(
    suspended:LambdaType.Suspended,
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
    suspended: LambdaType.Suspended, block: suspend ()-> R
):R {
    try {
        return block()
    }catch (throwable: Throwable){
        val exceptionTrace = throwable.extractTrace()
        if(throwable is TrackableException){
            val context =  currentCoroutineContext()
            throwable.coroutineInfo = context.coroutineInfo(throwable.contextClass, exceptionTrace.bestPick.methodName)
        }

        warn("delegateIfThrow",  throwable)
        ExceptionLocator.throwableRegistry.dispatch(throwable, suspended)
    }
}