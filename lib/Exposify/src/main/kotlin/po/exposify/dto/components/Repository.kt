package po.exposify.dto.components

import po.exposify.dto.DTOBase
import po.exposify.dto.components.relation_binder.MultipleChildContainer
import po.exposify.dto.components.relation_binder.SingleChildContainer
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.RootDTO
import po.exposify.dto.components.proFErty_binder.containerize
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.testOrThrow
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import po.misc.types.getOrThrow
import kotlin.collections.forEach



internal suspend fun<DTO : ModelDTO, DATA: DataModel, ENTITY: ExposifyEntity> selectDto(
    dtoClass: RootDTO<DTO, DATA>,
    entity: ENTITY
):CommonDTO<DTO, DATA, ENTITY>{
    val dto = dtoClass.config.dtoFactory.createDto()
    dto.updatePropertyBinding(entity, UpdateMode.ENTITY_TO_MODEL, entity.containerize(UpdateMode.ENTITY_TO_MODEL))
    dto.getDtoRepositories().forEach { it.select(entity) }
    return dto.castOrOperationsEx("selectDto. Cast failed.")
}

internal suspend fun <DTO : ModelDTO, DATA: DataModel, ENTITY: ExposifyEntity> updateDto(
    dtoClass: RootDTO<DTO, DATA>,
    dataModel: DATA
):CommonDTO<DTO, DATA, ENTITY>
{
    val dto = dtoClass.config.dtoFactory.createDto(dataModel)
    if(dataModel.id == 0L){
        dto.daoService.save(dto.castOrOperationsEx("updateDto(save). Cast failed."))
    }else{
        dto.daoService.update(dto.castOrOperationsEx("updateDto(update). Cast failed."))
    }
    dto.getDtoRepositories().forEach {repository->
        repository.update()
    }
    return dto.castOrOperationsEx("updateDto(Return). Cast failed.")
}


class SingleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE>(
    val binding : SingleChildContainer<DTO, DATA, ENTITY, C_DTO,  CD, CE>,
    val hostingDto : CommonDTO<DTO, DATA, ENTITY>,
    childClassConfig: DTOConfig<C_DTO, CD, CE>,
    val childClass: DTOBase<C_DTO, CD>,
): RepositoryBase<DTO,DATA, ENTITY, C_DTO, CD, CE>(childClassConfig), TasksManaged
        where DTO : ModelDTO, DATA: DataModel, ENTITY : ExposifyEntity,
              C_DTO : ModelDTO, CD: DataModel, CE: ExposifyEntity
{
    override val personalName: String  get() = "SingleRepository[${hostingDto.dtoName}]"

    fun getDTO(): CommonDTO<C_DTO, CD, CE>{
        return childDTOList.firstOrNull()
            .getOrThrow<CommonDTO<C_DTO, CD, CE>, OperationsException>(
                "Unable to get dto in $personalName",
                ExceptionCode.ABNORMAL_STATE.value)
    }

    suspend fun updateSingle() {
        val dataModel = binding.getDataModel(hostingDto.dataModel)
        val newChildDto = childFactory.createDto(dataModel)
        if (dataModel.id == 0L) {
            childDaoService.saveWithParent(newChildDto, hostingDto){containerized->
                binding.attachForeignEntity(containerized)
            }
        } else {
            childDaoService.update(newChildDto)
        }
        childDTOList.add(newChildDto)
        newChildDto.getDtoRepositories().forEach {repository->
            repository.update()
        }
    }

    suspend fun selectSingle(parentEntity: ENTITY) {
        val childEntity = binding.getChildEntity(parentEntity)
        val newChildDto = childFactory.createDto()
        newChildDto.updatePropertyBinding(childEntity, UpdateMode.ENTITY_TO_MODEL, childEntity.containerize(UpdateMode.ENTITY_TO_MODEL))

        binding.saveDataModel(newChildDto.dataModel)
        childDTOList.add(newChildDto)
        newChildDto.getDtoRepositories().forEach {repository->
             repository.select(childEntity)
       }
    }

    override suspend fun clear(): SingleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE>{
        childDTOList.clear()
        return this
    }
}

class MultipleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE>(
    val binding : MultipleChildContainer<DTO, DATA, ENTITY, C_DTO,  CD, CE>,
    val hostingDto : CommonDTO<DTO, DATA, ENTITY>,
    childClassConfig: DTOConfig<C_DTO, CD, CE>,
    val  childClass: DTOBase<C_DTO, CD>,
): RepositoryBase<DTO,DATA, ENTITY, C_DTO, CD, CE>(childClassConfig), TasksManaged
        where DTO : ModelDTO, DATA: DataModel, ENTITY : ExposifyEntity,
              C_DTO : ModelDTO, CD: DataModel, CE: ExposifyEntity
{
    override val personalName: String  get() = "MultipleRepository[${hostingDto.dtoName}]"

    suspend fun updateMultiple(): Unit = subTask("Update", personalName){ handler->

        val dataModels = binding.getDataModels(hostingDto.dataModel)
        handler.info("Update for parent dto ${hostingDto.dtoName} and id ${hostingDto.id} ")
        handler.info("Data Models count :${dataModels.count()} received from property ${binding.delegateName}")
        dataModels.forEach {dataModel->
            val newChildDto =  childFactory.createDto(dataModel)
            if(dataModel.id == 0L){
                childDaoService.saveWithParent(newChildDto, hostingDto){containerized->
                    binding.attachForeignEntity(containerized)
                }
            }else{
                childDaoService.update(newChildDto)
            }
            childDTOList.add(newChildDto)
            newChildDto.getDtoRepositories().forEach {repository->
                repository.update()
            }
        }
    }.resultOrException()

    suspend fun selectMultiple(parentEntity: ENTITY){
        val dtos = binding.getForeignEntities(parentEntity).map { entity ->
            childFactory.createDto().also { dto ->
                dto.updatePropertyBinding(entity, UpdateMode.ENTITY_TO_MODEL, entity.containerize(UpdateMode.ENTITY_TO_MODEL))
                dto.getDtoRepositories().forEach { it.select(entity) }
            }
        }
        binding.saveDataModels(dtos.map { it.dataModel })
        childDTOList.addAll(dtos)
    }

    fun getDTO(): List<CommonDTO<C_DTO, CD, CE>>{
        return childDTOList.toList().testOrThrow(
            OperationsException(
                "Dto list empty in $personalName",
                ExceptionCode.ABNORMAL_STATE)){
                it.count() != 0
        }
    }

    override suspend fun clear(): MultipleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE>{
        childDTOList.clear()
        return this
    }
}


sealed class RepositoryBase<DTO, DATA, ENTITY, C_DTO,  CD, CE>(
    protected val childClassConfig: DTOConfig<C_DTO, CD, CE>,
) where DTO : ModelDTO, DATA: DataModel, ENTITY : ExposifyEntity,
        C_DTO : ModelDTO, CD: DataModel, CE: ExposifyEntity
{

    abstract val personalName: String
    var initialized: Boolean = false

    protected val childDTOList: MutableList<CommonDTO<C_DTO, CD, CE>> = mutableListOf()

    internal val childFactory: DTOFactory<C_DTO, CD, CE>
        get() { return childClassConfig.dtoFactory }

    internal val childDaoService: DAOService<C_DTO, CD, CE>
        get() { return childClassConfig.daoService }

    suspend fun update(){
        when(this){
            is MultipleRepository-> updateMultiple()
            is SingleRepository-> updateSingle()
        }
    }

    suspend fun select(entity: ENTITY){
        when(this){
            is MultipleRepository-> selectMultiple(entity)
            is SingleRepository->selectSingle(entity)
        }
        initialized = true
    }

    abstract suspend fun clear():RepositoryBase<DTO, DATA, ENTITY, C_DTO,  CD, CE>

}