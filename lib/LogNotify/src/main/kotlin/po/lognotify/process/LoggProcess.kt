package po.lognotify.process

import kotlinx.coroutines.AbstractCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


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


