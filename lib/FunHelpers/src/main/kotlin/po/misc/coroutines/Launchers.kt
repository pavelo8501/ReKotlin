package po.misc.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlin.coroutines.coroutineContext


@Suppress("FunctionName")
suspend fun <T, R> RunAsync(receiver : T, block: suspend T.()-> R):R{
    return CoroutineScope(coroutineContext).async(start = CoroutineStart.DEFAULT){
        block.invoke(receiver)
    }.await()
}