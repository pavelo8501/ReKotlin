package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.bindings.BindingHub
import po.exposify.dto.components.bindings.helpers.newDTO
import po.exposify.dto.components.bindings.helpers.shallowDTO
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.toResult
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.DTOTracker
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.managedPayload
import po.exposify.exceptions.operationsException
import po.exposify.extensions.getOrOperations
import po.lognotify.TasksManaged
import po.lognotify.classes.notification.LoggerDataProcessor
import po.misc.callbacks.CallbackManager
import po.misc.interfaces.CtxId
import po.misc.interfaces.IdentifiableContext




sealed class ExecutionContext<DTO, DATA, ENTITY>(
   val dtoClass: DTOBase<DTO, DATA, ENTITY>
):IdentifiableContext, TasksManaged
        where DTO: ModelDTO, DATA : DataModel, ENTITY: LongEntity
{

    override val contextName: String = "ExecutionProvider"
    abstract val logger: LoggerDataProcessor
    private val daoService: DAOService<DTO, DATA, ENTITY> get() = dtoClass.config.daoService

    fun insert(data: DATA): ResultSingle<DTO, DATA, ENTITY> {
        println("$contextName insert triggered with data ${data.id}")
        val operation = CrudOperation.Insert
        if (dtoClass is RootDTO){
           val dto = dtoClass.newDTO().hub.loadHierarchyByData(data, dtoClass)
            return dto.toResult(operation)
        } else {
           throw operationsException(managedPayload("Setup misconfiguration", ExceptionCode.ABNORMAL_STATE))
        }
    }

    fun pickById(id: Long): ResultSingle<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Pick
        return dtoClass.lookupDTO(id)?.toResult(operation)?:run {
            val result = dtoClass.config.daoService.pickById(id)
            if(result != null){
                dtoClass.newDTO().hub.loadHierarchyByEntity(result, dtoClass).toResult(operation)
            }else{
                val message = "Entity with provided id :${id} not found"
                dtoClass.shallowDTO().toResult(OperationsException(message, ExceptionCode.DB_CRUD_FAILURE, null), operation)
            }
        }
    }

    fun pick(conditions: SimpleQuery): ResultSingle<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Pick

        val entity = dtoClass.config.daoService.pick(conditions)
        return if (entity != null) {
            dtoClass.newDTO().hub.loadHierarchyByEntity(entity, dtoClass).toResult(operation)
        } else {
            val queryStr = conditions.build().toSqlString()
            val message =
                "Unable to find ${dtoClass.config.registry.getRecord<DTO>(SourceObject.DTO)} for query $queryStr"
            dtoClass.shallowDTO().toResult(OperationsException(message, ExceptionCode.DB_CRUD_FAILURE, null), operation)
        }
    }

    fun select(): ResultList<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Select
        val entities =  dtoClass.config.daoService.select()
        return entities.map {
            val dto = dtoClass.newDTO()
            dto.hub.loadHierarchyByEntity(it, dtoClass)
        }.toResult(dtoClass, operation)
    }

    fun select(conditions: SimpleQuery): ResultList<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Select
        val entities = daoService.select(conditions)
        val dtos = entities.map {
            dtoClass.lookupDTO(it.id.value) ?: run {
                val dto = dtoClass.newDTO()
                dto.hub.loadHierarchyByEntity(it, dtoClass)
            }
        }
        return dtos.toResult(dtoClass, operation)
    }

    fun <T : IdTable<Long>> select(conditions: WhereQuery<T>): ResultList<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Select
        val entities =  daoService.select(conditions)
        val dtos = entities.map {
            dtoClass.lookupDTO(it.id.value)?: run {
                val dto = dtoClass.newDTO()
                dto.hub.loadHierarchyByEntity(it, dtoClass)
            }
        }
        return dtos.toResult(dtoClass, operation)
    }

    fun update(dataModel: DATA, initiator: CtxId): ResultSingle<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Update
        if (dataModel.id == 0L) {
            return insert(dataModel)
        }
        val existentDto = dtoClass.lookupDTO(dataModel.id)

        return if (existentDto != null) {
            existentDto.hub.updatePropertiesBy(dataModel).toResult(operation)
        } else {
            val dto = dtoClass.newDTO()
            dto.hub.loadHierarchyByData(dataModel, initiator).toResult(operation)
        }
    }

    fun updateSingle(dataModel: DATA, initiator: CtxId): ResultSingle<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Update
        if (dataModel.id == 0L) {
            return insert(dataModel)
        }
        val existentDto = dtoClass.lookupDTO(dataModel.id)

        return if (existentDto != null) {
            existentDto.hub.updatePropertiesBy(dataModel).toResult(operation)
        } else {
            val dto = dtoClass.newDTO()
            dto.hub.loadHierarchyByData(dataModel, initiator).toResult(operation)
        }
    }

    fun update(dataModels: List<DATA>,  initiator: CtxId): ResultList<DTO, DATA, ENTITY> {
        return dataModels.map { update(it, initiator) }.toResult(dtoClass)
    }

    fun insert(dataModels: List<DATA>): ResultList<DTO, DATA, ENTITY> {
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

class RootExecutionContext<DTO, D, E>(
   rootClass: RootDTO<DTO, D, E>,
   var sourceContext: CtxId? = null
):ExecutionContext<DTO, D, E>(rootClass) where DTO: ModelDTO , D: DataModel, E: LongEntity {
    override val logger: LoggerDataProcessor
        get() = logHandler.dataProcessor


    override val contextName: String
        get() = "RootExecutionContext[${sourceContext?.contextName?:super.contextName}]"

    override fun toString(): String{
        return contextName
    }
}

fun <DTO: ModelDTO, D: DataModel, E:LongEntity> RootDTO<DTO, D, E>.createProvider(
):RootExecutionContext<DTO, D, E> {
    return RootExecutionContext(this)
}



class DTOExecutionContext<DTO, D, E, F, FD, FE>(
    private val dto: CommonDTO<DTO, D, E>,
    private val foreignDtoClass: DTOBase<F, FD, FE>
):ExecutionContext<F, FD, FE>(foreignDtoClass)
        where DTO: ModelDTO, D: DataModel,E: LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity {
    enum class DTOExecutionEvents{
        OnDTOComplete
    }
    override val contextName: String = "DTOExecutionProvider"
    override val logger: LoggerDataProcessor get() = logHandler.dataProcessor

    val selfAsBase:ExecutionContext<F, FD, FE> get(){
        return this as ExecutionContext<F, FD, FE>
    }


    internal val notifier: CallbackManager<DTOExecutionEvents> = CallbackManager(DTOExecutionEvents::class.java, this)
    internal val onDTOComplete = CallbackManager.createPayload<DTOExecutionEvents, CommonDTO<DTO, D, E>>(
        notifier,
        DTOExecutionEvents.OnDTOComplete
    )

    internal var entityBacking:E? = null
    internal fun getEntity(initiated: CtxId):E{
        return  entityBacking.getOrOperations("EntityBacking", initiated)
    }

    private var dataModelBacking: D? = null
    internal fun provideDataModel(dataModel:D, initiated: CtxId){
        dataModelBacking = dataModel
    }
    internal fun getDataModel(initiated: CtxId):D{
      return  dataModelBacking.getOrOperations("DataModelBacking", initiated)
    }
    internal fun getDataModelOrNull():D?{
        return dataModelBacking
    }
    internal fun notifyDTOComplete(dto: CommonDTO<DTO, D, E>){
        onDTOComplete.triggerForAll(dto)
    }

    internal val tracker: DTOTracker<DTO, D, E> = dto.tracker
    internal val hub : BindingHub<DTO, D, E> get() = dto.hub

}


fun <DTO: ModelDTO, D: DataModel, E:LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity > createDTOProvider(
     dto: CommonDTO<DTO, D, E>,
     foreignDtoClass: DTOClass<F, FD, FE>
):DTOExecutionContext<DTO, D, E, F, FD, FE> {
    return DTOExecutionContext(dto, foreignDtoClass)
}