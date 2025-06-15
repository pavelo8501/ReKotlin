package po.exposify.scope.connection.controls

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.interfaces.RunnableContext
import po.exposify.scope.sequence.classes.ClassSequenceHandler
import po.exposify.scope.sequence.classes.RootSequenceHandler
import po.lognotify.process.runProcess


class CoroutineEmitter(
    val name: String,
    val session : AuthorizedSession
){
    suspend fun <DTO, D, E>dispatchRoot(rootHandler : RootSequenceHandler<DTO, D, E>): ResultList<DTO, D, E>
            where DTO : ModelDTO,  D: DataModel, E: LongEntity {
        return session.runProcess("Sequence dispatched by ${rootHandler.dtoRoot.completeName}", Dispatchers.IO) {
            newSuspendedTransaction(coroutineContext) {
                val runnableContext = RunnableContext.runInfo(session)
                rootHandler.handlerConfig.onStartCallback?.invoke(runnableContext)
                val result = rootHandler.launch(runnableContext)
                rootHandler.handlerConfig.onCompleteCallback?.invoke(runnableContext)
                result
            }
        }
    }

    suspend fun <DTO, D, E, F_DTO, FD, FE>dispatchChild(
        classHandler : ClassSequenceHandler<DTO, D, E, F_DTO, FD, FE>,
    ): ResultList<DTO, D, E>
    where DTO : ModelDTO,  D: DataModel, E: LongEntity,
                F_DTO: ModelDTO,FD : DataModel, FE : LongEntity {

        return session.runProcess("Sequence dispatched by ${classHandler.dtoClass.completeName}", Dispatchers.IO) {
            newSuspendedTransaction(coroutineContext){
                    val runnableContext = RunnableContext.runInfo(session)
                    classHandler.handlerConfig.onStartCallback?.invoke(runnableContext)
                    classHandler.handlerConfig.rootHandler.launch(runnableContext)
                    val result = classHandler.finalResult
                    classHandler.handlerConfig.onCompleteCallback?.invoke(runnableContext)
                    result
                }
        }
    }

}