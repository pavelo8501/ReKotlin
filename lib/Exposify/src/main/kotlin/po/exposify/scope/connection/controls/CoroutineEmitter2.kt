package po.exposify.scope.connection.controls

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import po.auth.sessions.interfaces.EmmitableSession
import po.auth.sessions.models.AuthorizedSession
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.models.SequencePack
import po.lognotify.extensions.newTask
import po.lognotify.extensions.startTask
import po.misc.exceptions.CoroutineInfo
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class CoroutineEmitter2(
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