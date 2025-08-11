package po.misc.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext


interface TaskLauncher{


    suspend fun < R> RunCoroutineHolder(
        context: CoroutineHolder,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        block: suspend CoroutineScope.() -> R
    ): R


}

sealed class LauncherType(){

        object AsyncLauncher: LauncherType(), TaskLauncher{

            override suspend fun <R> RunCoroutineHolder(
                context: CoroutineHolder,
                dispatcher: CoroutineDispatcher,
                block: suspend CoroutineScope.() -> R
            ): R = withContext(context.coroutineContext + dispatcher) {
                block()
            }
        }

        object ConcurrentLauncher : LauncherType(), TaskLauncher{

            override suspend fun < R> RunCoroutineHolder(
                context: CoroutineHolder,
                dispatcher: CoroutineDispatcher,
                block: suspend CoroutineScope.() -> R
            ): R {
                return  CoroutineScope(context.coroutineContext + dispatcher).async(start = CoroutineStart.UNDISPATCHED) {
                    block()
                }.await()
            }
        }

    fun selectLauncher(type: LauncherType):TaskLauncher{
       return when(type){
            is ConcurrentLauncher -> ConcurrentLauncher
            is AsyncLauncher -> AsyncLauncher
        }
    }

}

interface CoroutineHolder{
    val coroutineContext : CoroutineContext
}
