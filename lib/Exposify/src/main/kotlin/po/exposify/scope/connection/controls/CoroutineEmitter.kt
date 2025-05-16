package po.exposify.scope.connection.controls

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.components.ResultList
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.interfaces.RunnableContext
import po.exposify.scope.sequence.classes.ClassSequenceHandler
import po.exposify.scope.sequence.classes.RootSequenceHandler
import po.lognotify.extensions.launchProcess
import po.lognotify.extensions.newTask
import po.misc.exceptions.getCoroutineInfo


class CoroutineEmitter(
    val name: String,
    val session : AuthorizedSession
){
    suspend fun <DTO, D, E>dispatchRoot(rootHandler : RootSequenceHandler<DTO, D, E>): ResultList<DTO, D, E>
            where DTO : ModelDTO,  D: DataModel, E: LongEntity
    {
        return session.launchProcess {
            session.sessionContext.newTask("Sequence launch(dispatchRoot)", "CoroutineEmitter") {
               var result =  ResultList(rootHandler.dtoRoot)
                newSuspendedTransaction(Dispatchers.IO) {
                    result = rootHandler.launch(session)
                }
                val runnableContext = RunnableContext.createRunInfo(coroutineContext[AuthorizedSession], getCoroutineInfo())
                rootHandler.handlerConfig.onCompleteCallback?.invoke(runnableContext)
                result
            }.resultOrException()
        }
    }

    suspend fun <DTO, D, E, F_DTO, FD, FE>dispatchChild(
        classHandler : ClassSequenceHandler<DTO, D, E, F_DTO, FD, FE>,
    ): ResultList<DTO, D, E>
    where DTO : ModelDTO,  D: DataModel, E: LongEntity,
                F_DTO: ModelDTO,FD : DataModel, FE : LongEntity {

        return session.launchProcess {
            session.sessionContext.newTask("Sequence launch(dispatchChild)", "CoroutineEmitter") {
                var result =  ResultList(classHandler.dtoClass)
                newSuspendedTransaction(Dispatchers.IO) {
                    classHandler.handlerConfig.rootHandler.launch(session)
                    result = classHandler.finalResult
                }
                val runnableContext = RunnableContext.createRunInfo(coroutineContext[AuthorizedSession], getCoroutineInfo())
                classHandler.handlerConfig.onCompleteCallback?.invoke(runnableContext)
                result
            }.resultOrException()
        }
    }

}