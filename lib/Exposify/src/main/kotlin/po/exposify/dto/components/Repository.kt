package po.exposify.dto.components

import po.exposify.binders.relationship.BindingContainer2
import po.exposify.binders.relationship.MultipleChildContainer2
import po.exposify.binders.relationship.SingleChildContainer2
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.classes.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.countEqualsOrWithThrow
import po.lognotify.TasksManaged
import po.lognotify.extensions.countEqualsOrThrow
import po.lognotify.extensions.getOrThrowDefault
import po.lognotify.extensions.resultOrDefault
import po.lognotify.extensions.safeCast
import po.lognotify.extensions.subTask
import kotlin.collections.forEach

typealias ChildDTO<CHILD_DTO> = CommonDTO<CHILD_DTO, DataModel, ExposifyEntityBase>

class SingleRepository<DTO, DATA, ENTITY,  CHILD_DTO>(
    val hostingDto : CommonDTO<DTO, DATA, ENTITY>,
    val childClass: DTOClass<CHILD_DTO>,
    val binding : SingleChildContainer2<DTO, DATA, ENTITY, CHILD_DTO>
): RepositoryBase<DTO, DATA, ENTITY, CHILD_DTO>(childClass)
        where DTO: ModelDTO, CHILD_DTO: ModelDTO,  DATA: DataModel, ENTITY : ExposifyEntityBase {
    override val personalName: String = "Repository[${hostingDto.personalName}/Single]"

    suspend fun updateSingle() {
        val dataModel = binding.sourcePropertyWrapper.get(hostingDto.dataModel).getOrThrowDefault("Failed to get data model")
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
}

class MultipleRepository<DTO, DATA, ENTITY, CHILD_DTO>(
    val hostingDto : CommonDTO<DTO, DATA, ENTITY>,
    val childClass: DTOClass<CHILD_DTO>,
    val binding : MultipleChildContainer2<DTO, DATA, ENTITY, CHILD_DTO>,
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
            val newChildDto = childFactory.createDto(entity)
            childDTOList.add(newChildDto)
            newChildDto.getDtoRepositories().forEach {repository->
                repository.select()
            }
        }
   }
}

class RootRepository<DTO, DATA, ENTITY, CHILD_DTO>(
    val dtoClass: DTOClass<CHILD_DTO>,
): RepositoryBase<DTO,DATA, ENTITY, CHILD_DTO>(dtoClass), TasksManaged
        where DTO : ModelDTO, CHILD_DTO : ModelDTO, DATA: DataModel, ENTITY : ExposifyEntityBase
{
    override val personalName: String = "Repository[${dtoClass.registryItem.commonDTOKClass.simpleName}/Root]"

    suspend fun update(dataModels : List<DATA>): List<CommonDTO<DTO, DATA, ENTITY>>{
        val result = mutableListOf<CommonDTO<DTO, DATA, ENTITY>>()
        dataModels.forEach {dataModel->
           val castedDto = dtoClass.config.dtoFactory.createDto(dataModel).run {
               safeCast<CommonDTO<DTO, DATA, ENTITY>>().getOrThrowDefault("Cast to CommonDTO<DTO, DATA, ENTITY> failed")
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
            val castedDto = dtoClass.config.dtoFactory.createDto(entity).run {
                safeCast<CommonDTO<DTO, DATA, ENTITY>>().getOrThrowDefault("Cast to CommonDTO<DTO, DATA, ENTITY> failed")
            }
            castedDto.getDtoRepositories().forEach { repository ->
                repository.select()
            }
            result.add(castedDto)
        }
        return result
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
            else -> getOrThrowDefault("UpdateDTOs should have never reached this branch")
        }
    }

    suspend fun select(){
        when(this){
            is MultipleRepository-> selectMultiple()
            is SingleRepository->updateSingle()
            else -> getOrThrowDefault("SelectDTOs should have never reached this branch")
        }
    }


//    protected suspend fun <CHILD_DATA: DataModel> updateChildDtos(
//        dataModels : List<CHILD_DATA>,
//        parentDTO: CommonDTO<DTO, DATA, ENTITY>,
//        binding : BindingContainer2<DTO, DATA, ENTITY, CHILD_DTO>):List<ChildDTO<CHILD_DTO>> =
//    subTask("UpdateByDataModels", personalName){  handler->
//        handler.info("Creating child DTOs")
//        val createdDtos =  mutableListOf<ChildDTO<CHILD_DTO>>()
//        dataModels.forEach { dataModel ->
//            val newDto =  childFactory.createDto(dataModel).getOrThrowDefault("DTO creation by data failed @ $personalName")
//            val parentEntity =  parentDTO.daoService.getLastEntity()
//            newDto.daoService.saveWithParent{
//                binding.foreignEntityProperty.set(it, parentEntity)
//            }
//
//            createdDtos.add(newDto)
//        }
//        createdDtos.toList()
//    }.resultOrDefault(emptyList<ChildDTO<CHILD_DTO>>())

//    protected suspend fun sharedUpdateByDataModels(dataModels : List<DataModel>):List<ChildDTO<CHILD_DTO>> =
//        subTask("UpdateByDataModels", personalName){handler->
//            handler.info("Creating child DTOs")
//            val createdDtos =  mutableListOf<CommonDTO<CHILD_DTO,  DataModel, ExposifyEntityBase>>()
//            dataModels.forEach {dataModel->
//                val newDto = childFactory.createDto(dataModel).getOrThrowDefault("DTO creation by data failed @ $personalName")
//                createdDtos.add(newDto)
//            }
//            handler.info("Dtos created ${createdDtos.count()}")
//            updateDtos(createdDtos)
//    }.resultOrDefault(emptyList())

    suspend fun selectDtos(dtoList: List<ChildDTO<CHILD_DTO>>): List<ChildDTO<CHILD_DTO>>{
        dtoList.forEach {dto->
            dto.apply {
                daoService.trackedProperties.values.forEach {entityModelProperty->
                    val bindingContainer = entityModelProperty.getContainer()
                    val childEntities = daoService.extractChildEntities(entityModelProperty)
                    repositories.map[bindingContainer.thisKey].getOrThrowDefault("Child repository not found")
                        .sharedSelectByEntities(childEntities)
                }
            }
        }
        initialized = true
        return dtoList
    }
    protected suspend fun sharedSelectByEntities(entities: List<ENTITY>): List<ChildDTO<CHILD_DTO>> =
        subTask("SelectByEntities", personalName){
        val createdDtos =  mutableListOf<CommonDTO<CHILD_DTO,  DataModel, ExposifyEntityBase>>()
        entities.forEach { entity ->
            val newDto = childFactory.createDto(entity).getOrThrowDefault("DTO creation by entity failed @ $personalName")
            createdDtos.add(newDto)
        }
        selectDtos(createdDtos)
    }.resultOrDefault(emptyList())
}