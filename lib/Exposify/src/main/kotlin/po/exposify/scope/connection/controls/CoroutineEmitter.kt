package po.exposify.scope.connection.controls

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import po.exposify.classes.interfaces.DataModel
import po.exposify.scope.sequence.models.SequencePack

class CoroutineEmitter(
    val name: String,
){

//    fun <DATA : DataModel, ENTITY : LongEntity>dispatch(
//        pack: SequencePack<DATA, ENTITY>,
//        listenerScope : CoroutineScope
//    ): Deferred<List<DATA>> {
//
//        return listenerScope.async {
//            suspendedTransactionAsync(Dispatchers.IO) {
//                pack.start().await()
//            }.await()
//        }.also { deferred ->
//            deferred.invokeOnCompletion { throwable ->
//                if (throwable != null) {
//                    throw throwable
//                }
//            }
//        }
//    }
}