package po.exposify.scope.connection.controls

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.lognotify.process.runProcess

class CoroutineEmitter(
    val name: String,
    val session : AuthorizedSession
){

    suspend fun <DTO:ModelDTO, D: DataModel, E: LongEntity>dispatchSingle(block:suspend ()-> ResultSingle<DTO,D,E>): ResultSingle<DTO, D, E>{
        return session.runProcess("Sequence dispatch", Dispatchers.IO){
            newSuspendedTransaction(coroutineContext) {
                block.invoke()
            }
        }
    }

    suspend fun <DTO:ModelDTO, D: DataModel, E: LongEntity>dispatchList(block:suspend ()-> ResultList<DTO,D,E>): ResultList<DTO, D, E>{
        return session.runProcess("Sequence dispatch", Dispatchers.IO){
            newSuspendedTransaction(coroutineContext) {
                block.invoke()
            }
        }
    }
}