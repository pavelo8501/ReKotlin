package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
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
import po.lognotify.classes.task.TaskHandler


class ExecutionProvider<DTO, DATA, ENTITY>(
    override val dtoClass: DTOBase<DTO, DATA, ENTITY>,
):  ExecutionContext<DTO, DATA, ENTITY> where  DTO  : ModelDTO , DATA : DataModel, ENTITY: LongEntity {

    override val contextName: String = "ExecutionProvider"

    override val logger: TaskHandler<*> get() = taskHandler()

    private fun insert(data: DATA): ResultSingle<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Insert
        if (dtoClass is RootDTO) {
            return dtoClass.createDTO(data, operation).toResult(operation)
        } else {
            throwOperations("Setup misconfiguration", ExceptionCode.ABNORMAL_STATE)
        }
    }

    override fun pickById(id: Long): ResultSingle<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Pick
        val existent = dtoClass.lookupDTO(id, operation)
        return if (existent != null) {
            existent.toResult(operation)
        } else {
            dtoClass.config.daoService.pickById(id)?.let { entity ->
                entity.select(dtoClass, operation)
            } ?: run {
                val message = "Entity with provided id :${id} not found"
                dtoClass.shallowDTO().toResult(OperationsException(message, ExceptionCode.DB_CRUD_FAILURE, null), operation)
            }
        }
    }

    override fun pick(conditions: SimpleQuery): ResultSingle<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Pick
        return dtoClass.config.daoService.pick(conditions)?.let { entity ->
            val existent = dtoClass.lookupDTO(entity.id.value, operation)
            if (existent != null) {
                existent.toResult(operation)
            } else {
                entity.select(dtoClass, operation)
            }
        } ?: run {
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


    override fun update(dataModel: DATA): ResultSingle<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Update

        // 1. Redirect to insert if id == 0
        if (dataModel.id == 0L) {
            return insert(dataModel)
        }
        // 2. Try updating from cached/lookup DTO
        dtoClass.lookupDTO(dataModel.id, operation)
            ?.updateFromData(dataModel, operation)
            ?.let { return it }


        // 3. Try fetching the entity directly
        val entity = dtoClass.config.daoService.pickById(dataModel.id)
        if (entity != null) {
            return dtoClass.newDTO(entity).updateFromData(dataModel, operation)
        }

        // 4. Final fallback: return error
        val message = "Unable to update. DTO with id: ${dataModel.id} not found."
        return dtoClass.newDTO(dataModel)
            .toResult(OperationsException(message, ExceptionCode.DB_CRUD_FAILURE, null), operation)
    }

    override fun update(dataModels: List<DATA>): ResultList<DTO, DATA, ENTITY> {
        return dataModels.map { update(it) }.toResult(dtoClass)
    }
}

fun <DTO, DATA, ENTITY> DTOBase<DTO, DATA, ENTITY>.createExecutionProvider()
: ExecutionProvider<DTO, DATA, ENTITY>
        where  DTO  : ModelDTO , DATA : DataModel, ENTITY: LongEntity{
   return ExecutionProvider(this)
}