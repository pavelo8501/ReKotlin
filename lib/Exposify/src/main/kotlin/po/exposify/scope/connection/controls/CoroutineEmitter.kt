package po.exposify.scope.connection.controls

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.classes.interfaces.DataModel
import po.db.data_service.scope.sequence.models.SequencePack
import kotlin.coroutines.CoroutineContext

class CoroutineEmitter(
    val name: String,
   // val originatorContext : CoroutineContext
){
   fun <DATA : DataModel, ENTITY : LongEntity>dispatch(
       pack: SequencePack<DATA, ENTITY>){
       val listenerScope = CoroutineScope(Dispatchers.IO + CoroutineName(name))
       val job = listenerScope.launch {
           println("Pre launching Coroutine for pack ${pack.name}")
           pack.start()
           println("Launch")
       }
       job.invokeOnCompletion {
           println("Dispatcher $name is closing")
       }
    }
}