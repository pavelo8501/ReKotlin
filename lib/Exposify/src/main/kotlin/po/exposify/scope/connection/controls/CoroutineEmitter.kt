package po.exposify.scope.connection.controls

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import po.auth.sessions.interfaces.EmmitableSession
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.models.SequencePack
import po.lognotify.extensions.newTask


class CoroutineEmitter(
    val name: String,
){
   suspend fun <DTO, DATA>dispatch(
        pack: SequencePack<DTO, DATA>,
        session : EmmitableSession
    ):List<DATA>  where DTO:ModelDTO, DATA: DataModel  {
     return session.sessionContext.newTask("Sequence launch as startTask", "CoroutineEmitter2"){
            suspendedTransactionAsync(Dispatchers.IO){
                pack.start()
            }.await()
        }.resultOrException()
    }
}