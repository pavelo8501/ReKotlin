package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.bindings.helpers.newDTO
import po.exposify.dto.components.bindings.helpers.shallowDTO
import po.exposify.dto.components.query.SimpleQuery
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.query.toSqlString
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.toResult
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.lognotify.TasksManaged
import po.lognotify.notification.LoggerDataProcessor
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.context.asSubIdentity
import po.misc.functions.subscribers.subscribe


sealed class ExecutionContext<DTO, DATA, ENTITY>(
   val dtoClass: DTOBase<DTO, DATA, ENTITY>
): TasksManaged where DTO: ModelDTO, DATA : DataModel, ENTITY: LongEntity
{
    override val identity: CTXIdentity<out CTX> = asSubIdentity(this, dtoClass)

    private val daoService: DAOService<DTO, DATA, ENTITY> get() = dtoClass.dtoConfiguration.daoService

    fun insert(data: DATA): ResultSingle<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Insert
        if (dtoClass is RootDTO){
           val dto = dtoClass.newDTO().hub.loadHierarchyByData(data, dtoClass)
           return dto.toResult(operation)
        } else {
            throw operationsException("Setup misconfiguration", ExceptionCode.ABNORMAL_STATE, this)
        }
    }

    fun pickById(id: Long): ResultSingle<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Pick
        return dtoClass.lookupDTO(id)?.toResult(operation)?:run {
            val result = dtoClass.dtoConfiguration.daoService.pickById(id)
            if(result != null){
                dtoClass.newDTO().hub.loadHierarchyByEntity(result, dtoClass).toResult(operation)
            }else{
                val exception = operationsException("Entity with provided id :${id} not found",  ExceptionCode.DB_CRUD_FAILURE, this)
                dtoClass.toResult(exception)
            }
        }
    }

    fun pick(conditions: SimpleQuery): ResultSingle<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Pick

        val entity = dtoClass.dtoConfiguration.daoService.pick(conditions)
        return if (entity != null) {
            dtoClass.newDTO().hub.loadHierarchyByEntity(entity, dtoClass).toResult(operation)
        } else {
            val queryStr = conditions.build().toSqlString()
            val message = "Unable to find ${dtoClass.commonDTOType.dtoType} for query $queryStr"
            val exception = operationsException("Unable to find ${dtoClass.commonDTOType.dtoType} for query $queryStr", ExceptionCode.DB_CRUD_FAILURE, this)
            dtoClass.toResult(exception)
        }
    }

    fun select(): ResultList<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Select
        val entities =  dtoClass.dtoConfiguration.daoService.select()
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

    fun select(conditions: WhereQuery<ENTITY>): ResultList<DTO, DATA, ENTITY> {
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

    fun update(dataModel: DATA, initiator: CTX): ResultSingle<DTO, DATA, ENTITY> {
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

    fun updateSingle(dataModel: DATA, initiator: CTX): ResultSingle<DTO, DATA, ENTITY> {
        val operation = CrudOperation.Update
        if (dataModel.id == 0L) {
            return insert(dataModel)
        }
        val existentDto = dtoClass.lookupDTO(dataModel.id)

        return if (existentDto != null) {
            val updatedDTO =  existentDto.hub.updatePropertiesBy(dataModel)
            updatedDTO.toResult(operation)
        } else {
            val dto = dtoClass.newDTO()
            dto.hub.loadHierarchyByData(dataModel, initiator).toResult(operation)
        }
    }

    fun update(dataModels: List<DATA>,  initiator: CTX): ResultList<DTO, DATA, ENTITY> {
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
   rootClass: RootDTO<DTO, D, E>
):ExecutionContext<DTO, D, E>(rootClass) where DTO: ModelDTO , D: DataModel, E: LongEntity {

    override val identity: CTXIdentity<RootExecutionContext<DTO, D, E>> = asSubIdentity(this, rootClass)

    override fun toString(): String = completeName
}


/**
 * Here dtoClass is a child class
 * hostingDTO parent Common DTO
 */
class DTOExecutionContext<DTO, D, E, F, FD, FE>(
    dtoClass: DTOBase<DTO, D, E>,
    val hostingDTO: CommonDTO<F, FD, FE>,
    val dtoFactory: CommonDTOFactory<DTO, D, E, F, FD, FE> = CommonDTOFactory(hostingDTO, dtoClass.dtoConfiguration)
):ExecutionContext<DTO, D, E>(dtoClass)
        where DTO: ModelDTO, D: DataModel,E: LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity {

    override val identity: CTXIdentity<DTOExecutionContext<DTO, D, E, F, FD, FE>> = asSubIdentity(this,  hostingDTO)

    init {
        dtoFactory.onDTOCreated.subscribe(this, DTOFactoryBase.Events.OnCreated, ::onNewDTOCreated)
    }

    fun onNewDTOCreated(commonDTO: CommonDTO<DTO, D, E>){
        commonDTO.registerParentDTO(hostingDTO)
        println("Created $commonDTO")
    }

    override fun toString(): String = completeName
}

internal fun <DTO, D, E, F, FD, FE> CommonDTO<F, FD, FE>.createDTOContext(
    dtoClass: DTOClass<DTO, D, E>
):DTOExecutionContext<DTO, D, E, F, FD, FE>
where DTO: ModelDTO, D: DataModel, E:LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity {
    return DTOExecutionContext(dtoClass, this)
}