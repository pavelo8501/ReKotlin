package po.exposify.scope.sequence.extensions

import org.jetbrains.exposed.dao.LongEntity
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.components.ResultList
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.SequenceContext
import po.exposify.scope.sequence.classes.ClassSequenceHandler
import po.exposify.scope.sequence.classes.RootHandlerProvider
import po.exposify.scope.sequence.classes.RootSequenceHandler
import po.exposify.scope.sequence.classes.SwitchHandlerProvider
import po.exposify.scope.service.ServiceContext


context(serviceContext: ServiceContext<DTO, D, E>)
suspend fun <DTO, D, E> sequence(
    handlerDelegate : RootHandlerProvider<DTO, D, E>,
    block: suspend context(AuthorizedSession)  SequenceContext<DTO, D, E>.(RootSequenceHandler<DTO, D, E>) -> ResultList<DTO, D, E>
) where DTO: ModelDTO, D:DataModel, E:LongEntity
{
    handlerDelegate.storeSequenceLambda(block)
}

suspend context(sequenceContext: SequenceContext<F_DTO, FD, FE>)
fun <DTO, D, E, F_DTO, FD, FE> switchContext(
    handlerDelegate : SwitchHandlerProvider<DTO, D, E, F_DTO,FD, FE>,
    block: suspend  SequenceContext<DTO, D, E>.(ClassSequenceHandler<DTO, D, E, F_DTO,FD, FE>)-> ResultList<DTO, D, E>
)   where  DTO: ModelDTO, D : DataModel, E : LongEntity,
           F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
{
    handlerDelegate.storeSwitchLambda(block)
}
