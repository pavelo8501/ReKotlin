package po.misc.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

sealed class LauncherType(){
        object AsyncLauncher: LauncherType(){
            @Suppress("FunctionName")
            suspend fun <R> RunCoroutineHolder(
                context: CoroutineHolder,
                dispatcher: CoroutineDispatcher = Dispatchers.Default,
                block: suspend CoroutineScope.() -> R
            ): R = withContext(context.coroutineContext + dispatcher) {
                block()
            }
        }

        object ConcurrentLauncher : LauncherType(){
            @Suppress("FunctionName")
            suspend fun <R> RunCoroutineHolder(
                context: CoroutineHolder,
                dispatcher: CoroutineDispatcher = Dispatchers.Default,
                block: suspend CoroutineScope.() -> R
            ): R {
                return  CoroutineScope(context.coroutineContext + dispatcher).async(start = CoroutineStart.UNDISPATCHED) {
                    block()
                }.await()
            }
        }
}

interface CoroutineHolder{
    val coroutineContext : CoroutineContext

}