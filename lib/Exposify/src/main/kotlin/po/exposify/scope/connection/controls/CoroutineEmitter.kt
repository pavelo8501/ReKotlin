package po.exposify.scope.connection.controls

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import po.auth.sessions.models.AuthorizedSession
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
   suspend fun <DTO, DATA, ENTITY, R>dispatch(
       pack: SequencePack<DTO, DATA, ENTITY>,
       rootHandlerBlock: (suspend RootSequenceHandler<DTO, DATA, ENTITY>.() -> Unit)? = null,
      // classHandlerBlock: (suspend ClassSequenceHandler<DTO, DATA, ENTITY>.() -> Unit)? = null
    ): R  where DTO : ModelDTO,  DATA: DataModel, ENTITY : LongEntity {

       return session.launchProcess {
           session.sessionContext.newTask("Sequence launch as startTask", "CoroutineEmitter2") {
               suspendedTransactionAsync(Dispatchers.IO) {
                   when(pack){
                       is RootSequencePack<DTO, DATA, ENTITY> ->{
                           rootHandlerBlock?.let {
                               pack.start<R>(it)
                           }?:run {
                               throw InitException("RootHandler configuration lambda is undefined", ExceptionCode.UNDEFINED)
                           }

                       }
//                       is ClassSequencePack<DTO, DATA, ENTITY> -> {
//                           classHandlerBlock?.let {
//                               pack.start<R>(it)
//                           }?:run {
//                               throw InitException("ClassHandler configuration lambda is undefined", ExceptionCode.UNDEFINED)
//                           }
//                       }
                   }
               }.await()
           }.resultOrException()
       }
   }
}