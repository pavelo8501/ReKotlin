package po.exposify.scope.connection.controls

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.scope.sequence.classes.ClassSequenceHandler
import po.exposify.scope.sequence.classes.RootSequenceHandler
import po.exposify.scope.sequence.classes.SequenceHandlerBase
import po.exposify.scope.sequence.models.ClassSequencePack
import po.exposify.scope.sequence.models.RootSequencePack
import po.exposify.scope.sequence.models.SequencePack
import po.lognotify.extensions.launchProcess
import po.lognotify.extensions.newTask


class CoroutineEmitter(
    val name: String,
    val session : AuthorizedSession
){
   suspend fun <DTO, DATA, R>dispatch(
       pack: SequencePack<DTO, DATA>,
       rootHandlerBlock: (suspend RootSequenceHandler<DTO, DATA>.() -> Unit)? = null,
       classHandlerBlock: (suspend ClassSequenceHandler<DTO, DATA>.() -> Unit)? = null
    ): R  where DTO:ModelDTO, DATA: DataModel {

       return session.launchProcess {
           session.sessionContext.newTask("Sequence launch as startTask", "CoroutineEmitter2") {
               suspendedTransactionAsync(Dispatchers.IO) {
                   when(pack){
                       is RootSequencePack<DTO, DATA> ->{
                           rootHandlerBlock?.let {
                               pack.start<R>(it)
                           }?:run {
                               throw InitException("RootHandler configuration lambda is undefined", ExceptionCode.UNDEFINED)
                           }

                       }
                       is ClassSequencePack<DTO, DATA> -> {
                           classHandlerBlock?.let {
                               pack.start<R>(it)
                           }?:run {
                               throw InitException("ClassHandler configuration lambda is undefined", ExceptionCode.UNDEFINED)
                           }
                       }
                   }
               }.await()
           }.resultOrException()
       }
   }
}