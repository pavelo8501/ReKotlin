package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.RootDTO
import po.exposify.dto.components.bindings.helpers.createDTO
import po.exposify.dto.components.bindings.helpers.newDTO
import po.exposify.dto.components.bindings.helpers.select
import po.exposify.dto.components.bindings.helpers.shallowDTO
import po.exposify.dto.components.bindings.helpers.updateFromData
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.toResult
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ExecutionContext
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.throwOperations
import po.lognotify.TasksManaged
import po.lognotify.classes.task.TaskHandler
import po.misc.interfaces.IdentifiableContext


class ExecutionProvider<DTO, DATA, ENTITY>(
    override val dtoClass: DTOBase<DTO, DATA, ENTITY>,
): ExecutionContext<DTO, DATA, ENTITY>, IdentifiableContext where  DTO: ModelDTO, DATA : DataModel, ENTITY: LongEntity {

    override val contextName: String = "ExecutionProvider"
    override val logger: TaskHandler<*> get() = taskHandler()

    private fun insert(data: DATA): ResultSingle<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Insert
        if (dtoClass is RootDTO) {
           val dto = dtoClass.newDTO(data).bindingHub.loadHierarchyByData(null)
            return dto.toResult(operation)
        } else {
            throwOperations("Setup misconfiguration", ExceptionCode.ABNORMAL_STATE)
        }
    }

    override fun pickById(id: Long, initiator: IdentifiableContext): ResultSingle<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Pick
        return dtoClass.lookupDTO(id)?.toResult(operation)?:run {
            val result = dtoClass.config.daoService.pickById(id)
            if(result != null){
                dtoClass.newDTO(result).bindingHub.loadHierarchyByEntity(null).toResult(operation)
            }else{
                val message = "Entity with provided id :${id} not found"
                dtoClass.shallowDTO().toResult(OperationsException(message, ExceptionCode.DB_CRUD_FAILURE, null), operation)
            }
        }
    }

    override fun pick(conditions: SimpleQuery): ResultSingle<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Pick

        val entity = dtoClass.config.daoService.pick(conditions)
        return if (entity != null) {
            dtoClass.newDTO(entity).bindingHub.loadHierarchyByEntity(null).toResult(operation)
        } else {
            val queryStr = conditions.build().toSqlString()
            val message =
                "Unable to find ${dtoClass.config.registry.getRecord<DTO>(SourceObject.DTO)} for query $queryStr"
            dtoClass.shallowDTO().toResult(OperationsException(message, ExceptionCode.DB_CRUD_FAILURE, null), operation)
        }
    }

    override fun select(): ResultList<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Select
        val entities =  dtoClass.config.daoService.select()
        return  entities.select(dtoClass, operation)
    }

    override fun select(conditions: SimpleQuery): ResultList<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Select
        val entities = dtoClass.config.daoService.select(conditions)
        return entities.select(dtoClass, operation)
    }

    override fun <T : IdTable<Long>> select(conditions: WhereQuery<T>): ResultList<DTO, DATA, ENTITY>{
        val operation = CrudOperation.Select
        val entities = dtoClass.config.daoService.select(conditions)
        return entities.select(dtoClass, operation)
    }

    override fun update(dataModel: DATA, initiator: IdentifiableContext): ResultSingle<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Update

        if (dataModel.id == 0L) {
            return insert(dataModel)
        }
        dtoClass.lookupDTO(dataModel.id)
            ?.updateFromData(dataModel, operation)
            ?.let { return it }

        val entity = dtoClass.config.daoService.pickById(dataModel.id)
        if (entity != null) {
            return dtoClass.newDTO(entity).updateFromData(dataModel, operation)
        }

        val message = "Unable to update. DTO with id: ${dataModel.id} not found."
        return dtoClass.newDTO(dataModel)
            .toResult(OperationsException(message, ExceptionCode.DB_CRUD_FAILURE, null), operation)
    }

    override fun update(dataModels: List<DATA>,  initiator: IdentifiableContext): ResultList<DTO, DATA, ENTITY> {
        return dataModels.map { update(it, initiator) }.toResult(dtoClass)
    }

    override fun insert(dataModels: List<DATA>): ResultList<DTO, DATA, ENTITY> {
        val result = dataModels.map { dataModel ->
            dataModel.id = 0
            insert(dataModel)
        }.toResult(dtoClass)
        return result
    }

    fun delete(dataModel: DATA): ResultSingle<DTO, DATA, ENTITY>{
        TODO("Not yet implemented")
    }

}

class DTOExecutionProvider<DTO, D, E>(
    override val dtoClass: DTOBase<DTO, D, E>,
    val dto: CommonDTO<DTO, D, E>
):  ExecutionContext<DTO, D, E>, TasksManaged where  DTO: ModelDTO , D: DataModel, E: LongEntity {

    override val contextName: String = "DTOExecutionProvider"
    override val logger: TaskHandler<*> get() = taskHandler()
    override fun select(): ResultList<DTO, D, E> {
        TODO("Not yet implemented")
    }

    override fun <T : IdTable<Long>> select(conditions: WhereQuery<T>): ResultList<DTO, D, E> {
        TODO("Not yet implemented")
    }

    override fun select(conditions: SimpleQuery): ResultList<DTO, D, E> {
        TODO("Not yet implemented")
    }

    override fun pickById(id: Long,  initiator: IdentifiableContext): ResultSingle<DTO, D, E> {
        TODO("Not yet implemented")
    }

    override fun pick(conditions: SimpleQuery): ResultSingle<DTO, D, E> {
        TODO("Not yet implemented")
    }

    override fun update(dataModels: List<D>,  initiator: IdentifiableContext): ResultList<DTO, D, E> {
        TODO("Not yet implemented")
    }

    override fun update(dataModel: D,  initiator: IdentifiableContext): ResultSingle<DTO, D, E> {
        TODO("Not yet implemented")
    }
    override fun insert(dataModels: List<D>): ResultList<DTO, D, E> {
        TODO("Not yet implemented")
    }
}

fun <DTO, DATA, ENTITY> DTOBase<DTO, DATA, ENTITY>.createExecutionProvider()
: ExecutionProvider<DTO, DATA, ENTITY>
        where  DTO  : ModelDTO , DATA : DataModel, ENTITY: LongEntity{
   return ExecutionProvider(this)
}