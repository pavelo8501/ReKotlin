package po.misc.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext


@Suppress("FunctionName")
suspend fun <T : CoroutineHolder, R> T.RunAsync(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    block: suspend CoroutineScope.() -> R
): R = withContext(this.scope.coroutineContext) {
    block()
}

@Suppress("FunctionName")
suspend fun <T : CoroutineHolder, R> T.RunConcurrent(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    block: suspend CoroutineScope.() -> R
): R {
    return  CoroutineScope(this.scope.coroutineContext + dispatcher).async(start = CoroutineStart.UNDISPATCHED) {
        block()
    }.await()
}

data class LauncherPayload<T, P, R>(
    val lambda : T.(P)->R,
    val receiver: T,
    val parameter: P
)

@Suppress("FunctionName")
suspend fun <T : CoroutineHolder, REC, P, R> T.RunAsync(
    payload: LauncherPayload<REC, P, R>,
    block: suspend CoroutineScope.() -> R
): R = withContext(this.scope.coroutineContext) {
    block()
}
