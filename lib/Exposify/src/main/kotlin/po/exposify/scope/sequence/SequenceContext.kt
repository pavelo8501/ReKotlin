package po.exposify.scope.sequence

import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.CrudResult
import po.exposify.dto.CommonDTO
import po.exposify.classes.DTOClass
import po.exposify.classes.select
import po.exposify.classes.update
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.extensions.WhereCondition
import po.exposify.extensions.isTransactionReady
import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.service.ServiceClass
import po.lognotify.TasksManaged
import po.lognotify.extensions.resultOrDefault
import po.lognotify.extensions.subTask


class SequenceContext<DTO, DATA>(
    private  val serviceClass : ServiceClass<DTO, DATA, ExposifyEntityBase>,
    private val dtoClass : DTOClass<DTO>,
    private val handler : SequenceHandler<DTO, DATA>
): TasksManaged where  DTO : ModelDTO, DATA : DataModel
{

    private val  personalName : String = "SequenceContext[${dtoClass.registryItem.commonDTOKClass.simpleName}]"
    private val connection: Database = serviceClass.connection

    private lateinit var lastResult : CrudResult<DTO, DATA>

    private fun dtos(): List<CommonDTO<DTO, DATA , ExposifyEntityBase>>{
        val result =   mutableListOf<CommonDTO<DTO, DATA, ExposifyEntityBase>>()
        lastResult.rootDTOs.forEach{ result.add(it) }
        return result
    }


    suspend fun checkout(withResult :  CrudResult<DTO, DATA>? = null): List<DATA> {
        return lastResult.getData()
    }


    suspend fun <T: IdTable<Long>> pick(
        conditions: WhereCondition<T>,
        block: (suspend SequenceContext<DTO, DATA>.(dtos: List<CommonDTO<DTO, DATA, ExposifyEntityBase>>)-> Unit)? = null
    ) {
        if (block != null) {
            this.block(dtos())
        } else {
            checkout(lastResult)
        }
    }

    suspend fun <T: IdTable<Long>> select(
        conditions: WhereCondition<T>?,
        block: (suspend SequenceContext<DTO, DATA>.(dtos: List<CommonDTO<DTO, DATA, ExposifyEntityBase>>)-> Deferred<List<DATA>>)? = null
    ) {
        subTask("Select", personalName) { handler ->
            if (!isTransactionReady()) {
                handler.warn("Transaction lost context")
            }
            lastResult = if (conditions != null) {
                dtoClass.select<T, DTO, DATA>(conditions)
            } else {
                dtoClass.select<DTO, DATA>()
            }
            if (block != null) {
                this.block(dtos())
            } else {
                checkout(lastResult)
            }
        }
    }

    suspend fun select(
        block: (suspend SequenceContext<DTO, DATA>.(dtos: List<CommonDTO<DTO, DATA, ExposifyEntityBase>>)-> Deferred<List<DATA>>)? = null
    ){
       subTask("Select", personalName) {handler->
            if (!isTransactionReady()) {
                handler.warn("Transaction lost context")
            }
            lastResult = dtoClass.select()
            if (block != null) {
                this.block(dtos())
            } else {
                checkout(lastResult)
            }
        }
    }

    suspend fun update(
        dataModels: List<DATA>,
        block: (suspend SequenceContext<DTO, DATA>.(dtos: List<CommonDTO<DTO, DATA, ExposifyEntityBase>>)-> Deferred<List<DATA>>)? = null
    ) {
        subTask("Update", personalName) { handler ->
            if (!isTransactionReady()) {
                handler.warn("Transaction lost context")
            }
            lastResult = dtoClass.update(dataModels)
            if (block != null) {
                this.block(dtos())
            } else {
                checkout(lastResult)
            }
        }
    }

}

