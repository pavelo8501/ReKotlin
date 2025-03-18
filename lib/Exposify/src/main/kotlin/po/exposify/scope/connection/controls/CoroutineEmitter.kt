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
import kotlin.reflect.KProperty1

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

    suspend fun <DATA : DataModel, ENTITY : LongEntity>dispatch(
        pack: SequencePack<DATA, ENTITY>,
        data : List<DATA>
    ): Deferred<List<DATA>> {
        val listenerScope = CoroutineScope(Dispatchers.IO + CoroutineName(name))
        return listenerScope.async {
            info("Pre launching Coroutine for pack ${pack.sequenceName()}")
            val transactionResult = suspendedTransactionAsync(Dispatchers.IO) {
                pack.start(emptyList(),data)
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

    suspend fun <DATA : DataModel, ENTITY : LongEntity>dispatchWithConditions(
        pack: SequencePack<DATA, ENTITY>,
        conditions: List<Pair<KProperty1<DATA, *>, Any?>>,
        data : List<DATA>
    ): Deferred<List<DATA>> {
        val listenerScope = CoroutineScope(Dispatchers.IO + CoroutineName(name))
        return listenerScope.async {
            info("Pre launching Coroutine for pack ${pack.sequenceName()}")
            val transactionResult = suspendedTransactionAsync(Dispatchers.IO) {
                pack.start(conditions, data)
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