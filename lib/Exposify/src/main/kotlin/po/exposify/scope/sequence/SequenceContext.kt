package po.exposify.scope.sequence

import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import po.exposify.classes.DTOBase
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.CrudResult
import po.exposify.dto.CommonDTO
import po.exposify.classes.DTOClass
import po.exposify.classes.RootDTO
import po.exposify.classes.extensions.select
import po.exposify.classes.extensions.update
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.extensions.WhereCondition
import po.exposify.extensions.isTransactionReady
import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.service.ServiceClass
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import kotlin.coroutines.coroutineContext

interface RunnableContext: SessionIdentified{
    val method: String
    val sessionType: SessionType

    companion object{

        data class RunInfo (
            override val method: String,
            override val sessionType: SessionType,
            override val sessionID: String = "N/A",
            override val remoteAddress: String = "N/A"
        ) : RunnableContext{

        }

        fun createRunInfo(method: String, session: AuthorizedSession?):RunnableContext{
            if(session!=null){
                return RunInfo(method, session.sessionType, session.sessionID, session.remoteAddress)
            }else{
                return RunInfo(method, SessionType.ANONYMOUS)
            }

        }
    }
}


class SequenceContext<DTO, DATA>(
    private  val serviceClass : ServiceClass<DTO, DATA, ExposifyEntity>,
    private val dtoClass : RootDTO<DTO, DATA>,
    private val handler : SequenceHandler<DTO, DATA>,
): TasksManaged where  DTO : ModelDTO, DATA : DataModel
{

    private val personalName : String = "SequenceContext[${dtoClass.config.registry.dtoName}]"
    private val connection: Database = serviceClass.connection

    private var lastResult : CrudResult<DTO, DATA> = CrudResult()

    private fun dtos(): List<CommonDTO<DTO, DATA , ExposifyEntity>>{
      return  lastResult.rootDTOs
    }

    private suspend fun notifyOnStart(method: String){
        val session =  coroutineContext[AuthorizedSession]
        handler.onStartCallback?.invoke( RunnableContext.createRunInfo(method, session))
    }

    private suspend fun notifyOnComplete(method: String){
        val session =  coroutineContext[AuthorizedSession]
        handler.onCompleteCallback?.invoke(RunnableContext.createRunInfo(method, session))
    }

    suspend fun checkout(withResult :  CrudResult<DTO, DATA>? = null): List<DATA> {
        return lastResult.getData()

    }

    suspend fun <T: IdTable<Long>> pick(
        conditions: WhereCondition<T>,
        block: (suspend SequenceContext<DTO, DATA>.(dto: CommonDTO<DTO, DATA, ExposifyEntity>?)-> Unit)? = null
    ) {
        notifyOnStart("pick")
        lastResult =  dtoClass.select<T, DTO, DATA>(conditions)

        if (block != null) {
            this.block(dtos().firstOrNull())
        } else {
            checkout(lastResult)
            notifyOnComplete("pick")
        }
    }

    suspend fun <T: IdTable<Long>> select(
        conditions: WhereCondition<T>?,
        block: (suspend SequenceContext<DTO, DATA>.(dtos: List<CommonDTO<DTO, DATA, ExposifyEntity>>)-> Deferred<List<DATA>>)? = null
    ) {
        subTask("Select", personalName) { handler ->
            if (!isTransactionReady()) {
                handler.warn("Transaction lost context")
            }
            notifyOnStart("select(With conditions)")
            lastResult = if (conditions != null) {
                dtoClass.select<T, DTO, DATA>(conditions)
            } else {
                dtoClass.select<DTO, DATA>()
            }
            if (block != null) {
                this.block(dtos())
                notifyOnComplete("select")
            } else {
                checkout(lastResult)
                notifyOnComplete("select")
            }
        }
    }

    suspend fun select(
        block: (suspend SequenceContext<DTO, DATA>.(dtos: List<CommonDTO<DTO, DATA, ExposifyEntity>>)-> Deferred<List<DATA>>)? = null
    ){
       subTask("Select", personalName) {handler->
            if (!isTransactionReady()) {
                handler.warn("Transaction lost context")
            }
           notifyOnStart("select")
            lastResult = dtoClass.select()
            if (block != null) {
                this.block(dtos())
                notifyOnComplete("select")
            } else {
                checkout(lastResult)
                notifyOnComplete("select")
            }
        }
    }

    suspend fun update(
        dataModels: List<DATA>,
        block: (suspend SequenceContext<DTO, DATA>.(dtos: List<CommonDTO<DTO, DATA, ExposifyEntity>>)-> Deferred<List<DATA>>)? = null)
    {
        subTask("Update", personalName) { handler ->
            if (!isTransactionReady()) {
                handler.warn("Transaction lost context")
            }
            notifyOnStart("update")
            lastResult = dtoClass.update<DTO, DATA, ExposifyEntity>(dataModels)
            if (block != null) {
                this.block(dtos())
                notifyOnComplete("update")
            } else {
                checkout(lastResult)
                notifyOnComplete("update")
            }
        }
    }

}

