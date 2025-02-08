package po.exposify.scope.connection.controls

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import po.exposify.classes.interfaces.DataModel
import po.exposify.scope.sequence.models.SequencePack
import kotlin.coroutines.CoroutineContext

class CoroutineEmitter(
    val name: String,
   // val originatorContext : CoroutineContext
){
   fun <DATA : DataModel, ENTITY : LongEntity>dispatch(
       pack: SequencePack<DATA, ENTITY>, data : List<DATA>?){
       val listenerScope = CoroutineScope(Dispatchers.IO + CoroutineName(name))
       val job = listenerScope.launch {
           println("Pre launching Coroutine for pack ${pack.sequenceName()}")

           val transactionResult = suspendedTransactionAsync(Dispatchers.IO) {
               pack.start(data)  // ✅ Now runs inside a proper coroutine transaction
           }

           transactionResult.await() // ✅ Waits for DB operation to complete before continuing

           println("Launch complete for ${pack.sequenceName()}")

           println("Launch")
       }
       job.invokeOnCompletion {
           println("Dispatcher $name is closing")
       }
    }
}