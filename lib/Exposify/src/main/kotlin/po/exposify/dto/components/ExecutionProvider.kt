package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.DTOBase
import po.exposify.dto.components.bindings.helpers.createDTO
import po.exposify.dto.components.bindings.helpers.newDTO
import po.exposify.dto.components.bindings.helpers.updateFromData
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.toResult
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ExecutionContext
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.ModuleType
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.lognotify.classes.task.TaskHandler
import po.lognotify.lastTaskHandler
import po.misc.interfaces.IdentifiableModule

class ExecutionProvider<DTO, DATA, ENTITY>(
    override val dtoClass: DTOBase<DTO, DATA, ENTITY>,
    val moduleType : ModuleType.ExecutionProvider = ModuleType.ExecutionProvider
): IdentifiableModule by moduleType, ExecutionContext<DTO, DATA, ENTITY> where  DTO  : ModelDTO , DATA : DataModel, ENTITY: LongEntity{


    override val logger : TaskHandler<*> get() = lastTaskHandler()

    private fun insert(data: DATA): ResultSingle<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Insert
        return  dtoClass.createDTO(data, operation).toResult(operation)
    }

    override fun pickById(id: Long): ResultSingle<DTO, DATA, ENTITY>{
        val operation = CrudOperation.Pick
        val existent = dtoClass.lookupDTO(id, operation)
        return if(existent != null){
            existent.toResult(operation)
        }else{
             dtoClass.config.daoService.pickById(id)?.let { entity ->
                 dtoClass.createDTO(entity, operation).toResult(operation)
             }?:run {
                 val message = "Entity with provided id :${id} not found"
                 dtoClass.newDTO().toResult(OperationsException(message, ExceptionCode.DB_CRUD_FAILURE), operation)
             }
        }
    }
    override fun pick(conditions: SimpleQuery): ResultSingle<DTO, DATA, ENTITY>{
        val operation = CrudOperation.Pick
        return  dtoClass.config.daoService.pick(conditions)?.let { entity->
            val existent = dtoClass.lookupDTO(entity.id.value, operation)
            if(existent != null){
                existent.toResult(operation)
            }else{
                dtoClass.createDTO(entity, operation).toResult(operation)
            }
        }?:run {
            val queryStr = conditions.build().toSqlString()
            val message = "Unable to find ${dtoClass.config.registry.getRecord<DTO>(SourceObject.DTO)} for query $queryStr"
            dtoClass.newDTO().toResult(OperationsException(message, ExceptionCode.DB_CRUD_FAILURE), operation)
        }
    }

    override fun select(invalidateCache: Boolean): ResultList<DTO, DATA, ENTITY> {
        val operation  = CrudOperation.Select
        if(!invalidateCache){
            return dtoClass.lookupDTO().toResult(dtoClass, operation)
        }else{
            dtoClass.clearCachedDTOs()
            return dtoClass.createDTO(dtoClass.config.daoService.select(), operation).toResult(dtoClass, operation)
        }
    }
    override fun select(conditions: SimpleQuery): ResultList<DTO, DATA, ENTITY> {
        val operation  = CrudOperation.Select
        val entities =  dtoClass.config.daoService.select(conditions)
        return dtoClass.createDTO(entities, operation).toResult(dtoClass, operation)
    }
    override fun <T : IdTable<Long>> select(conditions: WhereQuery<T>): ResultList<DTO, DATA, ENTITY>
            = select(conditions)

    override fun update(dataModel: DATA): ResultSingle<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Update
        if (dataModel.id == 0L) {
            return insert(dataModel)
        } else {
            val result = dtoClass.lookupDTO(dataModel.id, operation)?.updateFromData(dataModel, operation)
            return result
                ?: (dtoClass.config.daoService.pickById(dataModel.id)?.let { entity ->
                    dtoClass.newDTO(entity).updateFromData(dataModel, operation)
                } ?: run {
                    val message = "Unable to update. DTO with id:${dataModel.id} not found."
                    dtoClass.newDTO(dataModel)
                        .toResult(OperationsException(message, ExceptionCode.DB_CRUD_FAILURE), operation)
                })
        }
    }
    override fun update(dataModels: List<DATA>): ResultList<DTO, DATA, ENTITY>{
        return  dataModels.map { update(it) }.toResult(dtoClass)
    }
}

fun <DTO, DATA, ENTITY> DTOBase<DTO, DATA, ENTITY>.createExecutionProvider()
: ExecutionProvider<DTO, DATA, ENTITY>
        where  DTO  : ModelDTO , DATA : DataModel, ENTITY: LongEntity{
   return ExecutionProvider(this)
}