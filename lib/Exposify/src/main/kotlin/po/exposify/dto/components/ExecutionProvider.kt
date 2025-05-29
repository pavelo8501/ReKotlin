package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.CommonDTO
import po.exposify.dto.RootDTO
import po.exposify.dto.components.bindings.helpers.createByEntity
import po.exposify.dto.components.proFErty_binder.containerize
import po.exposify.dto.components.bindings.property_binder.enums.UpdateMode
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.createResultList
import po.exposify.dto.components.result.createSingleResult
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.extensions.addTrackerInfo
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ExecutionContext
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.getOrOperationsEx
import po.lognotify.classes.task.TaskHandler
import po.lognotify.lastTaskHandler


class RootExecutionProvider<DTO, DATA, ENTITY>(
    override val dtoClass: RootDTO<DTO, DATA, ENTITY>,
): ExecutionContext<DTO, DATA, ENTITY> where  DTO  : ModelDTO , DATA : DataModel, ENTITY: LongEntity{

    override val qualifiedName: String
        get() = dtoClass.qualifiedName
    override val type: ComponentType = ComponentType.RootExecutionProvider

    override val logger : TaskHandler<*> get() = lastTaskHandler()

    private fun createDto(entity: ENTITY):CommonDTO<DTO, DATA, ENTITY>{
        val dto = dtoClass.config.dtoFactory.createDto()
        dto.bindingHub.updateEntity(entity)
        dtoClass.registerDTO(dto)
        dto.getDtoRepositories().forEach { it.loadHierarchyByEntity() }
        return dto
    }

    private fun insert(data: DATA): ResultSingle<DTO, DATA, ENTITY> {
        val dto = dtoClass.config.dtoFactory.createDto(data)
        dto.addTrackerInfo(CrudOperation.Insert, this)
        dto.createFromData()

        dtoClass.registerDTO(dto)
        return dto.createSingleResult(CrudOperation.Insert)
    }

    override fun select(): ResultList<DTO, DATA, ENTITY> {
        val dtos = mutableListOf<CommonDTO<DTO, DATA, ENTITY>>()
        val entities =  dtoClass.config.daoService.select()
        return  entities.createByEntity(dtoClass).createResultList(dtoClass, CrudOperation.Select)
    }
    override fun select(conditions: SimpleQuery): ResultList<DTO, DATA, ENTITY> {
        val dtos = mutableListOf<CommonDTO<DTO, DATA, ENTITY>>()
        val entities =  dtoClass.config.daoService.select(conditions)
        entities.forEach {
            val newDto =  createDto(it)
            dtos.add(newDto)
        }
        return dtos.createResultList(dtoClass, CrudOperation.Select)
    }

    override fun <T : IdTable<Long>> select(conditions: WhereQuery<T>): ResultList<DTO, DATA, ENTITY>
            = select(conditions)

    override fun pickById(id: Long): ResultSingle<DTO, DATA, ENTITY>{
        val existent = dtoClass.lookupDTO(id)
        return if(existent != null){
            ResultSingle(dtoClass, existent)
        }else{
            val entity = dtoClass.config.daoService.pickById(id).getOrOperationsEx("Entity with provided id :${id} not found")
            ResultSingle(dtoClass, createDto(entity))
        }
    }

    override fun pick(conditions: SimpleQuery): ResultSingle<DTO, DATA, ENTITY>{
        val entity = dtoClass.config.daoService.pick(conditions)
        if(entity == null){
            val queryStr = conditions.build().toSqlString()
            throw OperationsException("Unable to find ${dtoClass.config.registry.getSimpleName(ComponentType.DTO)} for query $queryStr",
                ExceptionCode.VALUE_NOT_FOUND)
        }else{
            val existent = dtoClass.lookupDTO(entity.id.value)
            return if (existent != null) {
                existent.addTrackerInfo(CrudOperation.Pick, this)
                existent.createSingleResult(CrudOperation.Pick)
            } else {
                createDto(entity).createSingleResult(CrudOperation.Initialize)
            }
        }
    }

    override fun update(dataModel: DATA): CommonDTO<DTO, DATA, ENTITY>{
        if (dataModel.id == 0L) {
            return insert(dataModel).getAsCommonDTOForced()
        } else {
            val existent = dtoClass.lookupDTO(dataModel.id)
            if (existent != null) {
                existent.addTrackerInfo(CrudOperation.Update, this)
               // existent.bindingHub.update(dataModel)
                existent.getDtoRepositories().forEach { repo ->
                    repo.loadHierarchyByModel()
                }
                return  existent
            } else {
                val entity = dtoClass.config.daoService.pickById(dataModel.id)
                    .getOrOperationsEx("Unable to update. DTO with id:${dataModel.id} not found.")
                val newDto = createDto(entity)
                newDto.addTrackerInfo(CrudOperation.Update, this)
                dtoClass.registerDTO(newDto)
              //  newDto.bindingHub.update(dataModel)
                return newDto
            }
        }
    }

    override fun update(dataModels: List<DATA>): ResultList<DTO, DATA, ENTITY>{
        val dtos = mutableListOf<CommonDTO<DTO, DATA, ENTITY>>()
        dataModels.forEach {
            dtos.add(update(it))
        }
        return dtos.createResultList(dtoClass, CrudOperation.Update)
    }
}

fun <DTO, DATA, ENTITY> RootDTO<DTO, DATA, ENTITY>.createExecutionProvider()
: RootExecutionProvider<DTO, DATA, ENTITY>
        where  DTO  : ModelDTO , DATA : DataModel, ENTITY: LongEntity{
   return RootExecutionProvider(this)
}