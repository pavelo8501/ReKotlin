package po.exposify.scope.connection.controls

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.models.SequencePack2

class CoroutineEmitter2(
    val name: String,
){

    fun <DTO: ModelDTO>dispatch(
        pack: SequencePack2<DTO>,
        listenerScope : CoroutineScope
    ): Deferred<List<DataModel>> {

        return listenerScope.async {
            suspendedTransactionAsync(Dispatchers.IO) {
                pack.start().await()
            }.await()
        }.also { deferred ->
            deferred.invokeOnCompletion { throwable ->
                if (throwable != null) {
                    throw throwable
                }
            }
        }
    }
}