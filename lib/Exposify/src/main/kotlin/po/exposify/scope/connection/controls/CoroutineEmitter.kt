package po.exposify.scope.connection.controls

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.lognotify.launchers.runProcess

class CoroutineEmitter(
    val name: String,
    val session : AuthorizedSession
){

    suspend fun <DTO:ModelDTO, D: DataModel>dispatchSingle(block:suspend ()-> ResultSingle<DTO, D>): ResultSingle<DTO, D>{
        return session.runProcess("Sequence dispatch", Dispatchers.IO){
            newSuspendedTransaction(coroutineContext) {
                block.invoke()
            }
        }
    }

    suspend fun <DTO:ModelDTO, D: DataModel>dispatchList(block:suspend ()-> ResultList<DTO, D>): ResultList<DTO, D>{
        return session.runProcess("Sequence dispatch", Dispatchers.IO){
            newSuspendedTransaction(coroutineContext) {
                block.invoke()
            }
        }
    }
}