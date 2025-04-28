package po.exposify.dto.components

import po.exposify.dto.components.relation_binder.MultipleChildContainer
import po.exposify.dto.components.relation_binder.SingleChildContainer
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.classes.DTOClass
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.getOrOperationsEx
import po.exposify.extensions.safeCast
import po.lognotify.TasksManaged
import po.misc.types.castOrThrow
import kotlin.collections.forEach


class SingleRepository<DTO, DATA, ENTITY,  CHILD_DTO>(
    val hostingDto : CommonDTO<DTO, DATA, ENTITY>,
    val childClass: DTOClass<CHILD_DTO>,
    val binding : SingleChildContainer<DTO, DATA, ENTITY, CHILD_DTO>
): RepositoryBase<DTO, DATA, ENTITY, CHILD_DTO>(childClass)
        where DTO: ModelDTO, CHILD_DTO: ModelDTO,  DATA: DataModel, ENTITY : ExposifyEntityBase {
    override val personalName: String = "Repository[${hostingDto.personalName}/Single]"

    suspend fun updateSingle() {
        val dataModel = binding.sourcePropertyWrapper.get(hostingDto.dataModel).getOrOperationsEx(
            "Failed to get data model", ExceptionCode.VALUE_NOT_FOUND)

        val newChildDto = childFactory.createDto(dataModel)
        if (dataModel.id == 0L) {
            val parentEntity = hostingDto.daoService.getLastEntity()
            newChildDto.daoService.saveWithParent {
                binding.foreignEntityProperty.set(it, parentEntity)
            }
        } else {
            newChildDto.daoService.update()
        }
        childDTOList.add(newChildDto)
        newChildDto.getDtoRepositories().forEach {repository->
            repository.update()
        }
    }

    override suspend fun clear(): SingleRepository<DTO, DATA, ENTITY, CHILD_DTO>{
        childDTOList.clear()
        return this
    }

}

class MultipleRepository<DTO, DATA, ENTITY, CHILD_DTO>(
    val hostingDto : CommonDTO<DTO, DATA, ENTITY>,
    val childClass: DTOClass<CHILD_DTO>,
    val binding : MultipleChildContainer<DTO, DATA, ENTITY, CHILD_DTO>,
): RepositoryBase<DTO, DATA, ENTITY, CHILD_DTO>(childClass)
    where DTO: ModelDTO, CHILD_DTO: ModelDTO, DATA: DataModel, ENTITY : ExposifyEntityBase {
   override val personalName: String = "Repository[${hostingDto.personalName}/Multiple]"

   suspend fun updateMultiple(){
        val dataModels = binding.ownDataModelsProperty.get(hostingDto.dataModel)
        dataModels.forEach {dataModel->
            val newChildDto =  childFactory.createDto(dataModel)
            if(dataModel.id == 0L){
                val parentEntity = hostingDto.daoService.getLastEntity()
                newChildDto.daoService.saveWithParent{
                    binding.foreignEntityProperty.set(it, parentEntity)
                }
            }else{
                newChildDto.daoService.update()
            }
            childDTOList.add(newChildDto)
            newChildDto.getDtoRepositories().forEach {repository->
                repository.update()
            }
        }
    }

   suspend fun selectMultiple(){
        val entities = binding.ownEntitiesProperty.get(hostingDto.daoService.getLastEntity())
        entities.forEach { entity ->
            val newChildDto = childFactory.createDto()
            newChildDto.updateBinding(entity, UpdateMode.ENTITY_TO_MODEL)
            val mutableList =  binding.ownDataModelsProperty.get(hostingDto.dataModel)
            mutableList.add(newChildDto.dataModel)
            childDTOList.add(newChildDto)
            newChildDto.getDtoRepositories().forEach {repository->
                repository.select()
            }
        }
   }

    override suspend fun clear(): MultipleRepository<DTO, DATA, ENTITY, CHILD_DTO>{
        childDTOList.clear()
        return this
    }
}

class RootRepository<DTO, DATA, ENTITY, CHILD_DTO>(
    val dtoClass: DTOClass<CHILD_DTO>,
): RepositoryBase<DTO,DATA, ENTITY, CHILD_DTO>(dtoClass), TasksManaged
        where DTO : ModelDTO, CHILD_DTO : ModelDTO, DATA: DataModel, ENTITY : ExposifyEntityBase
{
    override val personalName: String = "Repository[${dtoClass.registryItem.dtoClassName}/Root]"

    suspend fun update(dataModels : List<DATA>): List<CommonDTO<DTO, DATA, ENTITY>>{
        val result = mutableListOf<CommonDTO<DTO, DATA, ENTITY>>()
        dataModels.forEach {dataModel->
           val castedDto = dtoClass.config.dtoFactory.createDto(dataModel).run {
               safeCast<CommonDTO<DTO, DATA, ENTITY>>()
                   .getOrOperationsEx("Cast to CommonDTO<DTO, DATA, ENTITY> failed", ExceptionCode.CAST_FAILURE)
           }
           if(dataModel.id == 0L){
               castedDto.daoService.save()
           }else{
               castedDto.daoService.update()
           }
           castedDto.getDtoRepositories().forEach {repository->
                repository.update()
            }
            result.add(castedDto)
        }
        return result
    }

    suspend fun select(entities: List<ENTITY>): List<CommonDTO<DTO, DATA, ENTITY>>{
        val result = mutableListOf<CommonDTO<DTO, DATA, ENTITY>>()
        entities.forEach { entity ->
            val castedDto = dtoClass.config.dtoFactory.createDto().run {
                safeCast<CommonDTO<DTO, DATA, ENTITY>>()
                    .getOrOperationsEx("Cast to CommonDTO<DTO, DATA, ENTITY> failed", ExceptionCode.CAST_FAILURE)
            }
            castedDto.updateBinding(entity, UpdateMode.ENTITY_TO_MODEL)
            castedDto.getDtoRepositories().forEach { repository ->
                repository.select()
            }
            result.add(castedDto)
        }
        return result
    }

    suspend fun pick(uninitializedDto: CommonDTO<DTO, DATA, ENTITY>,  entity: ENTITY):CommonDTO<DTO, DATA, ENTITY>?{
        val initializedDto =  uninitializedDto.updateBinding(entity, UpdateMode.ENTITY_TO_MODEL)
        initializedDto.getDtoRepositories().forEach { repository ->
            repository.select()
        }
        return initializedDto
    }

    override suspend fun clear(): RootRepository<DTO, DATA, ENTITY, CHILD_DTO>{
        childDTOList.clear()
        return this
    }

}

sealed class RepositoryBase<DTO, DATA, ENTITY, CHILD_DTO>(
    private val childClass: DTOClass<CHILD_DTO>
) where DTO : ModelDTO, CHILD_DTO: ModelDTO, DATA: DataModel, ENTITY : ExposifyEntityBase {
    abstract val personalName: String
    var initialized: Boolean = false

    protected val childDTOList: MutableList<CommonDTO<CHILD_DTO, DataModel, ExposifyEntityBase>> = mutableListOf()

    internal val childFactory: DTOFactory<CHILD_DTO, DataModel, ExposifyEntityBase>
        get() { return childClass.config.dtoFactory }

    suspend fun update(){
        when(this){
            is MultipleRepository-> updateMultiple()
            is SingleRepository-> updateSingle()
            else -> getOrOperationsEx("UpdateDTOs should have never reached this branch", ExceptionCode.ABNORMAL_STATE)
        }
    }

    suspend fun select(){
        when(this){
            is MultipleRepository-> selectMultiple()
            is SingleRepository->updateSingle()
            else -> getOrOperationsEx("UpdateDTOs should have never reached this branch", ExceptionCode.ABNORMAL_STATE)
        }
        initialized = true
    }

    abstract suspend fun clear():RepositoryBase<DTO, DATA, ENTITY, CHILD_DTO>

}