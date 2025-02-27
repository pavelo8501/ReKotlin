package po.exposify.scope.connection.controls

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

   fun <DATA : DataModel, ENTITY : LongEntity>dispatch2(
       pack: SequencePack<DATA, ENTITY>,
       data : List<DATA>
   ) : Deferred<List<DATA>>  {

       val listenerScope = CoroutineScope(Dispatchers.IO + CoroutineName(name))
       val job = listenerScope.launch {
           info("Pre launching Coroutine for pack ${pack.sequenceName()}")
           val transactionResult = suspendedTransactionAsync(Dispatchers.IO) {
               pack.start(data)
           }
           transactionResult.await()
           info("Launch complete for ${pack.sequenceName()}")
       }
       job.invokeOnCompletion {throwable->
           if(throwable == null){
               info("Dispatcher $name is closing")
           }else{
               throwPropagate(throwable.message.toString())
           }
       }
       return CompletableDeferred(emptyList<DATA>())
    }

    suspend fun <DATA : DataModel, ENTITY : LongEntity>dispatch(
        pack: SequencePack<DATA, ENTITY>,
        data : List<DATA>
    ): Deferred<List<DATA>> {
        val listenerScope = CoroutineScope(Dispatchers.IO + CoroutineName(name))
        return listenerScope.async {
            info("Pre launching Coroutine for pack ${pack.sequenceName()}")
            val transactionResult = suspendedTransactionAsync(Dispatchers.IO) {
                pack.start(data)
                pack.onResult()
            }
            transactionResult.await()
        }.also { deferred ->
            deferred.invokeOnCompletion { throwable ->
                if (throwable == null) {
                    info("Dispatcher $name is closing")
                } else {
                    throwPropagate(throwable.message.toString())
                }
            }
        }
    }
}