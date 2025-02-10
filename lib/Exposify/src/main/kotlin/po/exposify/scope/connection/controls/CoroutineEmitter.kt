package po.exposify.scope.connection.controls

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import po.exposify.classes.interfaces.DataModel
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
import po.exposify.scope.sequence.models.SequencePack
import po.lognotify.eventhandler.EventHandler
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.eventhandler.interfaces.CanNotify

class CoroutineEmitter(
    val name: String,
    // val originatorContext : CoroutineContext
    var parentNotifier: RootEventHandler //Temporary solution unless sessions will get introduced
) : CanNotify {

    override val eventHandler = EventHandler(name, parentNotifier)

    init {
        eventHandler.registerPropagateException<OperationsException>{
            OperationsException("Operations exception", ExceptionCodes.LAZY_NOT_INITIALIZED)
        }
    }

   fun <DATA : DataModel, ENTITY : LongEntity>dispatch(
       pack: SequencePack<DATA, ENTITY>, data : List<DATA>?){

       val listenerScope = CoroutineScope(Dispatchers.IO + CoroutineName(name))
       val job = listenerScope.launch {
           info("Pre launching Coroutine for pack ${pack.sequenceName()}")
           val transactionResult = suspendedTransactionAsync(Dispatchers.IO) {
               pack.start(data)  // ✅ Now runs inside a proper coroutine transaction
           }
           transactionResult.await() // ✅ Waits for DB operation to complete before continuing
           info("Launch complete for ${pack.sequenceName()}")
       }
       job.invokeOnCompletion {throwable->
           if(throwable == null){
               info("Dispatcher $name is closing")
           }else{
               throwPropagate(throwable.message.toString())
           }
       }
    }
}