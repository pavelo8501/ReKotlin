package po.exposify.scope.connection.controls

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.scope.sequence.classes.ClassSequenceHandler
import po.exposify.scope.sequence.classes.RootSequenceHandler
import po.exposify.scope.sequence.models.RootSequencePack
import po.exposify.scope.sequence.models.SequencePack
import po.lognotify.extensions.launchProcess
import po.lognotify.extensions.newTask


class CoroutineEmitter(
    val name: String,
    val session : AuthorizedSession
){
   suspend fun <DTO, DATA, ENTITY, F_DTO, FD, FE, R>dispatch(
       pack: SequencePack<DTO, DATA, ENTITY>,
       rootHandlerBlock: (suspend RootSequenceHandler<DTO, DATA, ENTITY>.() -> Unit),
       childDtoClass: DTOClass<F_DTO, FD, FE>? = null,
    ): R  where DTO : ModelDTO,  DATA: DataModel, ENTITY : LongEntity,
            F_DTO: ModelDTO, FD: DataModel, FE : LongEntity{

       return session.launchProcess {
           session.sessionContext.newTask("Sequence launch as startTask", "CoroutineEmitter2") {
               newSuspendedTransaction(Dispatchers.IO) {
                   when(pack){
                       is RootSequencePack<DTO, DATA, ENTITY> ->{
                           if(childDtoClass != null){
                               pack.start(childDtoClass, rootHandlerBlock)
                           }else{
                               pack.start<R>(rootHandlerBlock)
                           }
                       }
                   }
               }
           }.resultOrException()
       }
   }
}