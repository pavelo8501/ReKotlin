package po.exposify.scope.sequence.extensions

import org.jetbrains.exposed.dao.LongEntity
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.ResultSingle
import po.exposify.dto.components.SwitchQuery
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperationsEx
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.ClassSequenceHandler
import po.exposify.scope.sequence.classes.RootHandlerProvider
import po.exposify.scope.sequence.classes.RootSequenceHandler
import po.exposify.scope.sequence.classes.SequenceHandlerBase
import po.exposify.scope.sequence.classes.SwitchHandlerProvider
import po.exposify.scope.service.ServiceContext
import po.lognotify.TasksManaged
import po.lognotify.extensions.lastTaskHandler


context(serviceContext: ServiceContext<DTO, D, E>)
suspend fun <DTO, D, E> sequence(
    handlerDelegate : RootHandlerProvider<DTO, D, E>,
    block: suspend context(AuthorizedSession)  SequenceContext<DTO, D, E>.(RootSequenceHandler<DTO, D, E>) -> ResultList<DTO, D, E>
) where DTO: ModelDTO, D:DataModel, E:LongEntity
{
    handlerDelegate.storeSequenceLambda(block)
}


//Should execute block lambda immediately. The process is already ongoing no need to store it.
suspend context(sequenceContext: SequenceContext<F_DTO,FD, FE>)
fun <DTO, D, E, F_DTO, FD, FE> switchContext(
    handlerDelegate : SwitchHandlerProvider<DTO, D, E, F_DTO,FD, FE>,
    switchLambda :  suspend  SequenceContext<DTO, D, E>.(ClassSequenceHandler<DTO, D, E, F_DTO, FD, FE>)-> ResultList<DTO, D, E>
) where  DTO: ModelDTO, D : DataModel, E : LongEntity,
           F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
{
    val switchHandler = sequenceContext.sequenceHandler.handlerConfig.getSwitchHandler(handlerDelegate.name)
    switchHandler?.let {
        val casted = it.castOrOperationsEx<ClassSequenceHandler<DTO, D, E, F_DTO, FD, FE>>()
        casted.launch(switchLambda)
    }?:run {
        lastTaskHandler().warn("Switch statement name: ${handlerDelegate.name} will not be executed. No handler being provided")
    }
}

context(sequenceContext: SequenceContext<DTO, D, E>)
fun <DTO, D, E> collectResult(
    result: ResultSingle<DTO, D, E>
): ResultSingle<DTO, D, E> where DTO: ModelDTO, D:DataModel, E:LongEntity{
    sequenceContext.sequenceHandler.provideCollectedResultSingle(result)
    return  result
}


context(sequenceContext: SequenceContext<DTO, D, E>)
@JvmName("collectResultList")
fun <DTO, D, E> collectResult(
    result: ResultList<DTO, D, E>
): ResultList<DTO, D, E> where DTO: ModelDTO, D:DataModel, E:LongEntity{
    sequenceContext.sequenceHandler.provideCollectedResultList(result)
    return result
}