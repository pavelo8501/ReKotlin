package po.exposify.scope.connection.controls

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import po.exposify.classes.interfaces.DataModel
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
import po.exposify.scope.sequence.models.SequencePack
import po.lognotify.eventhandler.EventHandler
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.eventhandler.interfaces.CanNotify
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KProperty1

class CoroutineEmitter(
    val name: String,
    var parentNotifier: RootEventHandler
) : CanNotify {

    override val eventHandler: EventHandler = EventHandler(name, parentNotifier)

    init {
        eventHandler.registerPropagateException<OperationsException>{
            OperationsException("Operations exception", ExceptionCodes.LAZY_NOT_INITIALIZED)
        }
    }

    suspend fun <DATA : DataModel, ENTITY : LongEntity>dispatch(
        pack: SequencePack<DATA, ENTITY>,
        listenerScope : CoroutineScope
    ): Deferred<List<DATA>> {

   //     val session = CoroutineSessionHolder.getCurrentContext(1)

        return listenerScope.async {
            info("Pre launching Coroutine for pack ${pack.sequenceName()}")
            suspendedTransactionAsync(Dispatchers.IO) {
                pack.start().await()
            }.await()
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