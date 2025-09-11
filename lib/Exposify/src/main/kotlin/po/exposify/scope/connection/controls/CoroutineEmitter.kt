package po.exposify.scope.connection.controls

import po.auth.sessions.models.AuthorizedSession
import po.exposify.common.classes.ExposifyDebugger
import po.exposify.common.classes.exposifyDebugger
import po.exposify.common.events.ContextData
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dao.transaction.withSuspendedTransactionIfNone
import po.lognotify.TasksManaged
import po.lognotify.process.Process
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity

class CoroutineEmitter(
    val name: String,
    val process : Process<AuthorizedSession>
): TasksManaged {

    override val identity: CTXIdentity<CoroutineEmitter> = asIdentity()

    val debugger: ExposifyDebugger<CoroutineEmitter, ContextData> =
        exposifyDebugger(this, ContextData) { ContextData(it.message) }

    suspend fun <R>dispatch(block:suspend ()-> R): R{
        return withSuspendedTransactionIfNone(debugger, warnIfNoTransaction = false, process.coroutineContext){
            block.invoke()
        }
    }
    suspend fun <DTO:ModelDTO, D: DataModel>dispatchSingle(block:suspend ()-> ResultSingle<DTO, D>): ResultSingle<DTO, D>{
        return  withSuspendedTransactionIfNone(debugger, warnIfNoTransaction = false, process.coroutineContext){
            block.invoke()
        }
    }

    suspend fun <DTO:ModelDTO, D: DataModel>dispatchList(block:suspend ()-> ResultList<DTO, D>): ResultList<DTO, D>{
        return  withSuspendedTransactionIfNone(debugger, warnIfNoTransaction = false, process.coroutineContext){
            block.invoke()
        }
    }
}