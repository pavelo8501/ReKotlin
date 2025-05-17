package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.CommonDTO
import po.exposify.dto.RootDTO
import po.exposify.dto.components.proFErty_binder.containerize
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.extensions.addTrackerInfo
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ExecutionContext
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.getOrOperationsEx
import po.lognotify.classes.task.TaskHandler
import po.lognotify.classes.task.TaskHandlerBase
import po.lognotify.lastTaskHandler


class RootExecutionProvider<DTO, DATA, ENTITY>(
    override val dtoClass: RootDTO<DTO, DATA, ENTITY>,
): ExecutionContext<DTO, DATA, ENTITY> where  DTO  : ModelDTO , DATA : DataModel, ENTITY: LongEntity{

    override val qualifiedName: String
        get() = dtoClass.qualifiedName
    override val type: ComponentType = ComponentType.RootExecutionProvider

    override val logger : TaskHandlerBase<*> get() = lastTaskHandler()

    private suspend fun createDto(entity: ENTITY):CommonDTO<DTO, DATA, ENTITY>{
        val dto = dtoClass.config.dtoFactory.createDto()
        dto.dtoPropertyBinder.update(entity.containerize(UpdateMode.ENTITY_TO_MODEL, null, true))
        dtoClass.registerDTO(dto)
        dto.getDtoRepositories().forEach { it.loadHierarchyByEntity() }
        return dto
    }

    private suspend fun insert(dataModel: DATA): ResultSingle<DTO, DATA, ENTITY> {
        val dto = dtoClass.config.dtoFactory.createDto(dataModel)
        dto.addTrackerInfo(CrudOperation.Insert, this)
        dtoClass.config.daoService.save(dto)
        dtoClass.registerDTO(dto)
        dto.getDtoRepositories().forEach { it.loadHierarchyByModel() }
        with(dtoClass) {
            return dto.createSingleResult(CrudOperation.Insert)
        }
    }

    override suspend fun select(): ResultList<DTO, DATA, ENTITY> {
        val result = ResultList<DTO, DATA, ENTITY>(dtoClass)
        val entities =    dtoClass.config.daoService.select()
        entities.forEach {
            val newDto =  createDto(it)
            result.appendDto(newDto)
        }
        return result
    }
    override suspend fun select(conditions: SimpleQuery): ResultList<DTO, DATA, ENTITY> {
        val result = ResultList<DTO, DATA, ENTITY>(dtoClass)
        val entities =  dtoClass.config.daoService.select(conditions)
        entities.forEach {
            val newDto =  createDto(it)
            result.appendDto(newDto)
        }
        return result
    }

    override suspend fun <T : IdTable<Long>> select(conditions: WhereQuery<T>): ResultList<DTO, DATA, ENTITY>
            = select(conditions)

    override suspend fun pickById(id: Long): ResultSingle<DTO, DATA, ENTITY>{
        val existent = dtoClass.lookupDTO(id)
        return if(existent != null){
            ResultSingle(dtoClass, existent)
        }else{
            val entity = dtoClass.config.daoService.pickById(id).getOrOperationsEx("Entity with provided id :${id} not found")
            ResultSingle(dtoClass, createDto(entity))
        }
    }

    override suspend fun pick(conditions: SimpleQuery): ResultSingle<DTO, DATA, ENTITY>{
        with(dtoClass) {
            val entity = dtoClass.config.daoService.pick(conditions)
                .getOrOperationsEx("Entity with provided query :${conditions} not found")
            val existent = dtoClass.lookupDTO(entity.id.value)
            return if (existent != null) {
                existent.addTrackerInfo(CrudOperation.Pick, this@RootExecutionProvider)
                existent.createSingleResult(CrudOperation.Pick)
            } else {
                createDto(entity).createSingleResult(CrudOperation.Initialize)
            }
        }
    }

    override suspend fun update(dataModel: DATA): ResultSingle<DTO, DATA, ENTITY>{
        with(dtoClass) {
            if (dataModel.id == 0L) {
                return insert(dataModel)
            } else {

                val existent = dtoClass.lookupDTO(dataModel.id)
                if (existent != null) {
                    existent.addTrackerInfo(CrudOperation.Update, this@RootExecutionProvider)
                    existent.dtoPropertyBinder.update(dataModel)
                    existent.getDtoRepositories().forEach { repo ->
                        repo.loadHierarchyByModel()
                    }
                    return  existent.createSingleResult(CrudOperation.Update)
                } else {
                    val entity = dtoClass.config.daoService.pickById(dataModel.id)
                        .getOrOperationsEx("Unable to update. DTO with id:${dataModel.id} not found.")
                    val newDto = createDto(entity)
                    newDto.addTrackerInfo(CrudOperation.Update, this@RootExecutionProvider)
                    dtoClass.registerDTO(newDto)
                    newDto.dtoPropertyBinder.update(dataModel)
                    return newDto.createSingleResult(CrudOperation.Update)
                }
            }
        }
    }

    override suspend fun update(dataModels: List<DATA>): ResultList<DTO, DATA, ENTITY>{
        val result =  ResultList(dtoClass)
        dataModels.forEach {
            result.appendDto(update(it))
        }
        return result
    }
}

fun <DTO, DATA, ENTITY> RootDTO<DTO, DATA, ENTITY>.createExecutionProvider()
: RootExecutionProvider<DTO, DATA, ENTITY>
        where  DTO  : ModelDTO , DATA : DataModel, ENTITY: LongEntity{
   return RootExecutionProvider(this)
}