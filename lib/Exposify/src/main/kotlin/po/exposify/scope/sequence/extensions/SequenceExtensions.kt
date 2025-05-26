package po.exposify.scope.sequence.extensions

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.castOrOperationsEx
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.ClassSequenceHandler
import po.exposify.scope.sequence.classes.RootHandlerProvider
import po.exposify.scope.sequence.classes.RootSequenceHandler
import po.exposify.scope.sequence.classes.SwitchHandlerProvider
import po.exposify.scope.service.ServiceContext
import po.lognotify.lastTaskHandler


fun <DTO, D, E>  ServiceContext<DTO, D, E>.sequence(
    handlerDelegate : RootHandlerProvider<DTO, D, E>,
    block: suspend  SequenceContext<DTO, D, E>.(RootSequenceHandler<DTO, D, E>) -> ResultList<DTO, D, E>
) where DTO: ModelDTO, D:DataModel, E:LongEntity
{
    this.dtoClass.reinitChild()
    handlerDelegate.storeSequenceLambda(block)
}

//Should execute block lambda immediately. The process is already ongoing no need to store it.
suspend fun <DTO, D, E, F_DTO, FD, FE> SequenceContext<F_DTO,FD, FE>.switchContext(
    handlerDelegate : SwitchHandlerProvider<DTO, D, E, F_DTO,FD, FE>,
    switchLambda :  suspend  SequenceContext<DTO, D, E>.(ClassSequenceHandler<DTO, D, E, F_DTO, FD, FE>)-> ResultList<DTO, D, E>
) where  DTO: ModelDTO, D : DataModel, E : LongEntity,
           F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
{
    val switchHandler = sequenceHandler.handlerConfig.getSwitchHandler(handlerDelegate.name)
    switchHandler?.let {
        val casted = it.castOrOperationsEx<ClassSequenceHandler<DTO, D, E, F_DTO, FD, FE>>()
        casted.launch(switchLambda)
    }?:run {
        lastTaskHandler().warn("Switch statement name: ${handlerDelegate.name} will not be executed. No handler being provided")
    }
}

fun <DTO, D, E> SequenceContext<DTO, D, E>.collectResult(
    result: ResultSingle<DTO, D, E>
): ResultSingle<DTO, D, E> where DTO: ModelDTO, D:DataModel, E:LongEntity{
    sequenceHandler.provideCollectedResultSingle(result)
    return  result
}


@JvmName("collectResultList")
fun <DTO, D, E>  SequenceContext<DTO, D, E>.collectResult(
    result: ResultList<DTO, D, E>
): ResultList<DTO, D, E> where DTO: ModelDTO, D:DataModel, E:LongEntity{
    sequenceHandler.provideCollectedResultList(result)
    return result
}