package po.db.data_service.transportation

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class CoroutineEmitter(
    val name: String,
    val originatorContext : CoroutineContext
) {
   fun dispatch(
        waypoint: String,
        block: (String) -> Unit
   ){
        val listenerScope = CoroutineScope(
            Dispatchers.IO + CoroutineName(name) + originatorContext
        )
        Job(
        ).invokeOnCompletion {
            val doOnClose: ()->Unit = {
                println("Dispatcher $name is closing")
            }
            listenerScope.launch {
                block(waypoint)

            }
        }
    }
}