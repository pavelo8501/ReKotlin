package po.exposify.scope.connection.controls

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.models.SequencePack

class CoroutineEmitter2(
    val name: String,
){
   suspend fun <DTO, DATA>dispatch(
        pack: SequencePack<DTO, DATA>,
        listenerScope : CoroutineScope
    ): Deferred<List<DATA>>  where DTO:ModelDTO, DATA: DataModel  {

      val result = listenerScope.async {

               suspendedTransactionAsync(Dispatchers.IO) {
                   pack.start()
               }
           }.await()
       return result
    }
}