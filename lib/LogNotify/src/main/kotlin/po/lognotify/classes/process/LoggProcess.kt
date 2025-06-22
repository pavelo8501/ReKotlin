package po.lognotify.classes.process

import kotlinx.coroutines.AbstractCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import po.lognotify.classes.task.RootTask
import po.lognotify.models.TaskKey
import po.lognotify.process.ProcessableContext
import po.misc.coroutines.CoroutineInfo
import po.misc.time.ExecutionTimeStamp
import po.misc.time.MeasuredContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext


interface ProcessScope: CoroutineScope{

}

@OptIn(InternalCoroutinesApi::class)
class  ProcessScopeImpl<T: ProcessableContext<E>, E: CoroutineContext.Element>(
    context: CoroutineContext,
    coroutineElement : T
): AbstractCoroutine<Unit>(context, initParentJob = true, active = true), ProcessScope {

}

fun <T: ProcessableContext<E>, E: CoroutineContext.Element> processScope(
    context: CoroutineContext = EmptyCoroutineContext,
    coroutineElement : T
): ProcessScope
{
    var scope: ProcessScopeImpl<T, E>? = null
    return ProcessScopeImpl<T, E>(context, coroutineElement).also { scope = it }
}


