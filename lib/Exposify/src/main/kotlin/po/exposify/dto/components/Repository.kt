package po.exposify.dto.components

import po.exposify.dto.DTOBase
import po.exposify.dto.components.relation_binder.MultipleChildContainer
import po.exposify.dto.components.relation_binder.SingleChildContainer
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.containerize
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import po.exposify.extensions.testOrThrow
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import po.misc.types.getOrThrow
import kotlin.collections.forEach


class SingleRepository<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(
    val binding : SingleChildContainer<DTO, DATA, ENTITY, CHILD_DTO,  CHILD_DATA, CHILD_ENTITY>,
    val hostingDto : CommonDTO<DTO, DATA, ENTITY>,
    val childClassConfig: DTOConfig<CHILD_DTO, CHILD_DATA, CHILD_ENTITY>,
    val childClass: DTOBase<CHILD_DTO, *>,
): RepositoryBase<DTO,DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(childClassConfig, childClass), TasksManaged
        where DTO : ModelDTO, DATA: DataModel, ENTITY : ExposifyEntity,
              CHILD_DTO : ModelDTO, CHILD_DATA: DataModel, CHILD_ENTITY: ExposifyEntity
{
    override val personalName: String  get() = "SingleRepository[${hostingDto.personalName}]"

    fun getDTO(): CommonDTO<CHILD_DTO, CHILD_DATA, CHILD_ENTITY>{
        return childDTOList.firstOrNull()
            .getOrThrow<CommonDTO<CHILD_DTO, CHILD_DATA, CHILD_ENTITY>, OperationsException>(
                "Unable to get dto in $personalName",
                ExceptionCode.ABNORMAL_STATE.value)
    }

    suspend fun updateSingle() {
        val dataModel = binding.getDataModel(hostingDto.dataModel)
        val newChildDto = childFactory.createDto(dataModel)
        if (dataModel.id == 0L) {
            childDaoService.saveWithParent(newChildDto, hostingDto){
                binding.setForeignEntity(hostingDto.daoEntity.containerize(UpdateMode.MODEL_TO_ENTITY))
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
        childDTOList.add(newChildDto)
        newChildDto.getDtoRepositories().forEach {repository->
            repository.select(childEntity)
        }
    }

    override suspend fun clear(): SingleRepository<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>{
        childDTOList.clear()
        return this
    }
}

class MultipleRepository<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(
    val binding : MultipleChildContainer<DTO, DATA, ENTITY, CHILD_DTO,  CHILD_DATA, CHILD_ENTITY>,
    val hostingDto : CommonDTO<DTO, DATA, ENTITY>,
    val childClassConfig: DTOConfig<CHILD_DTO, CHILD_DATA, CHILD_ENTITY>,
    val childClass: DTOBase<CHILD_DTO, *>,
): RepositoryBase<DTO,DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(childClassConfig, childClass), TasksManaged
        where DTO : ModelDTO, DATA: DataModel, ENTITY : ExposifyEntity,
              CHILD_DTO : ModelDTO, CHILD_DATA: DataModel, CHILD_ENTITY: ExposifyEntity
{
    override val personalName: String  get() = "MultipleRepository[${hostingDto.personalName}]"

    suspend fun updateMultiple() = subTask("Update", personalName){handler->

        val dataModels = binding.getDataModels(hostingDto.dataModel)
        handler.info("Update for parent dto ${hostingDto.personalName} and id ${hostingDto.id} ")
        handler.info("Data Models count :${dataModels.count()} received from property ${binding.delegateName}")
        dataModels.forEach {dataModel->
            val newChildDto =  childFactory.createDto(dataModel)
            if(dataModel.id == 0L){
                childDaoService.saveWithParent(newChildDto, hostingDto){
                    binding.setForeignEntity(hostingDto.daoEntity.containerize(UpdateMode.MODEL_TO_ENTITY))
                }
            }else{
                childDaoService.update(newChildDto)
            }
            childDTOList.add(newChildDto)
            newChildDto.getDtoRepositories().forEach {repository->
                repository.update()
            }
        }
    }

    suspend fun selectMultiple(parentEntity: ENTITY){

        val entities = binding.getChildEntities(parentEntity)
        entities.forEach { entity ->
            val newChildDto = childFactory.createDto()
            newChildDto.updatePropertyBinding(entity, UpdateMode.ENTITY_TO_MODEL, entity.containerize(UpdateMode.ENTITY_TO_MODEL))
            childDTOList.add(newChildDto)
            newChildDto.getDtoRepositories().forEach {repository->
                repository.select(entity)
            }
        }
    }

    fun getDTO(): List<CommonDTO<CHILD_DTO, CHILD_DATA, CHILD_ENTITY>>{
        return childDTOList.toList().testOrThrow(
            OperationsException(
                "Dto list empty in $personalName",
                ExceptionCode.ABNORMAL_STATE)){
                it.count() != 0
        }
    }

    override suspend fun clear(): MultipleRepository<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>{
        childDTOList.clear()
        return this
    }
}


sealed class RepositoryBase<DTO, DATA, ENTITY, CHILD_DTO,  CHILD_DATA, CHILD_ENTITY>(
    private val childClassConfig: DTOConfig<CHILD_DTO, CHILD_DATA, CHILD_ENTITY>,
    private val childClass: DTOBase<CHILD_DTO, *>,
) where DTO : ModelDTO, DATA: DataModel, ENTITY : ExposifyEntity,
        CHILD_DTO : ModelDTO, CHILD_DATA: DataModel, CHILD_ENTITY: ExposifyEntity
{

    abstract val personalName: String
    var initialized: Boolean = false

    protected val childDTOList: MutableList<CommonDTO<CHILD_DTO, CHILD_DATA, CHILD_ENTITY>> = mutableListOf()


    internal val childFactory: DTOFactory<CHILD_DTO, CHILD_DATA, CHILD_ENTITY>
        get() { return childClassConfig.dtoFactory }

    internal val childDaoService: DAOService<CHILD_DTO, CHILD_DATA, CHILD_ENTITY>
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

    abstract suspend fun clear():RepositoryBase<DTO, DATA, ENTITY, CHILD_DTO,  CHILD_DATA, CHILD_ENTITY>

}