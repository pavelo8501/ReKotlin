package po.exposify.scope.connection.controls

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import po.auth.sessions.interfaces.EmmitableSession
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.sequence.models.SequencePack
import po.lognotify.extensions.newTask


class CoroutineEmitter(
    val name: String,
    val session : EmmitableSession
){
   suspend fun <DTO, DATA>dispatch(
        handler: SequenceHandler<DTO, DATA>,
        pack: SequencePack<DTO, DATA>,
    )  where DTO:ModelDTO, DATA: DataModel{

     return session.sessionContext.newTask("Sequence launch as startTask", "CoroutineEmitter2"){
            suspendedTransactionAsync(Dispatchers.IO){
                pack.start(handler)
            }.await()
        }.resultOrException()
    }
}