package po.misc.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job


interface EmittableFlow<T: Any>{
    fun collectEmissions(scope: CoroutineScope, collector: suspend (T) -> Unit): Job
    fun stopBroadcast()
    fun emitData(data: T)
}
