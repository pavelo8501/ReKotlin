package po.exposify.scope.connection.controls

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import po.auth.sessions.models.AuthorizedSession
import po.exposify.common.classes.ExposifyDebugger
import po.exposify.common.classes.exposifyDebugger
import po.exposify.common.events.ContextData
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.withSuspendedTransactionIfNone
import po.lognotify.TasksManaged
import po.lognotify.launchers.runProcess
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.coroutines.coroutineInfo

class CoroutineEmitter(
    val name: String,
    val session : AuthorizedSession
): TasksManaged {

    override val identity: CTXIdentity<CoroutineEmitter> = asIdentity()

    val debugger: ExposifyDebugger<CoroutineEmitter, ContextData> =
        exposifyDebugger(this, ContextData) { ContextData(it.message) }

    suspend fun <DTO:ModelDTO, D: DataModel>dispatchSingle(block:suspend ()-> ResultSingle<DTO, D>): ResultSingle<DTO, D>{
        return runProcess(session, Dispatchers.IO){
            withSuspendedTransactionIfNone(debugger, warnIfNoTransaction = false, currentCoroutineContext()){
                block.invoke()
            }
        }
    }

    suspend fun <DTO:ModelDTO, D: DataModel>dispatchList(block:suspend ()-> ResultList<DTO, D>): ResultList<DTO, D>{
        return runProcess(session,  Dispatchers.IO){
            withSuspendedTransactionIfNone(debugger, warnIfNoTransaction = false, currentCoroutineContext()){
                block.invoke()
            }
        }
    }
}