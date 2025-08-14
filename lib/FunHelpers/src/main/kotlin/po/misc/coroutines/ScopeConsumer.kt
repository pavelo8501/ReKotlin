package po.misc.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

interface CoroutineHolder{
    val scope: CoroutineScope
}

interface TaskLauncher{
    suspend fun < R> RunCoroutineHolder(receiver: CoroutineHolder, block: suspend CoroutineScope.() -> R): R
}

sealed class LauncherType() {

    object ConcurrentLauncher : LauncherType(), TaskLauncher {
        override suspend fun <R> RunCoroutineHolder(
            receiver: CoroutineHolder,
            block: suspend CoroutineScope.() -> R
        ): R = withContext(receiver.scope.coroutineContext) {
            block()
        }
    }

    object AsyncLauncher : LauncherType(), TaskLauncher {
        override suspend fun <R> RunCoroutineHolder(
            receiver: CoroutineHolder,
            block: suspend CoroutineScope.() -> R
        ): R {
            return receiver.scope.async(start = CoroutineStart.UNDISPATCHED) {
                block()
            }.await()
        }
    }

    fun selectLauncher(type: LauncherType): TaskLauncher {
        return when (type) {
            is ConcurrentLauncher -> ConcurrentLauncher
            is AsyncLauncher -> AsyncLauncher
        }
    }
}