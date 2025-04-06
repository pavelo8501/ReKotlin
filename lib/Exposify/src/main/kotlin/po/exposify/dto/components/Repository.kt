package po.exposify.dto.components

import po.exposify.binders.relationship.BindingContainer2
import po.exposify.binders.relationship.MultipleChildContainer2
import po.exposify.binders.relationship.SingleChildContainer2
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.classes.DTOClass
import po.exposify.classes.components.DTOFactory
import po.exposify.dto.enums.CrudType
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.countEqualsOrWithThrow
import po.exposify.extensions.getOrThrow
import po.managedtask.exceptions.enums.CancelType
import kotlin.collections.forEach

typealias ChildDTO<CHILD_DTO> = CommonDTO<CHILD_DTO, DataModel, ExposifyEntityBase>

class SingleRepository<DTO, DATA, ENTITY,  CHILD_DTO>(
    val parent : CommonDTO<DTO, DATA, ENTITY>,
    val childClass: DTOClass<CHILD_DTO>,
    val bindingContainer : SingleChildContainer2<DTO, DATA, ENTITY, CHILD_DTO>
): RepositoryBase<DTO, DATA, ENTITY, CHILD_DTO>(childClass, bindingContainer)
        where DTO: ModelDTO, CHILD_DTO: ModelDTO,  DATA: DataModel, ENTITY : ExposifyEntityBase  {
    override val personalName: String = "Repository[${parent.personalName}/Single]"
}

class MultipleRepository<DTO, DATA, ENTITY, CHILD_DTO>(
    val parent : CommonDTO<DTO, DATA, ENTITY>,
    val childClass: DTOClass<CHILD_DTO>,
    val bindingContainer : MultipleChildContainer2<DTO, DATA, ENTITY, CHILD_DTO>,
): RepositoryBase<DTO, DATA, ENTITY, CHILD_DTO>(childClass, bindingContainer)
    where DTO: ModelDTO, CHILD_DTO: ModelDTO, DATA: DataModel, ENTITY : ExposifyEntityBase {

    override val personalName: String = "Repository[${parent.personalName}/Multiple]"
}

class RootRepository<DTO, DATA, ENTITY, CHILD_DTO>(
    val dtoClass: DTOClass<CHILD_DTO>,
): RepositoryBase<DTO,DATA, ENTITY, CHILD_DTO>(dtoClass) where DTO : ModelDTO, CHILD_DTO : ModelDTO, DATA: DataModel, ENTITY : ExposifyEntityBase
{
    override val personalName: String = "Repository[${dtoClass.registryItem.commonDTOKClass.simpleName}/Root]"

    suspend fun updateByDataModels(dataModels : List<DATA>): List<CommonDTO<CHILD_DTO, DATA, ENTITY>>{
         val createdDtoList =  sharedUpdateByDataModels(dataModels)
         val castedList =  createdDtoList.filterIsInstance<CommonDTO<CHILD_DTO, DATA, ENTITY>>().countEqualsOrWithThrow(createdDtoList.count()) {
             OperationsException("UpdateByDataModels Cast dtoList to actual after creation in $personalName failed", ExceptionCode.CAST_FAILURE)
         }
         return castedList
     }

    suspend fun selectByByEntities(entities: List<ENTITY>): List<CommonDTO<CHILD_DTO,  DATA, ENTITY>>{
        val createdDtoList =  sharedSelectByEntities(entities)
        val castedList =  createdDtoList.filterIsInstance<CommonDTO<CHILD_DTO, DATA, ENTITY>>().countEqualsOrWithThrow(createdDtoList.count()) {
            OperationsException("SelectByByEntities Cast dtoList to actual after creation in $personalName failed", ExceptionCode.CAST_FAILURE)
        }
        return castedList
    }
}


sealed class RepositoryBase<DTO, DATA, ENTITY, CHILD_DTO>(
    private val forClass: DTOClass<CHILD_DTO>,
    private val binding : BindingContainer2<DTO, DATA, ENTITY, CHILD_DTO>? = null
) where DTO : ModelDTO, CHILD_DTO: ModelDTO, DATA: DataModel, ENTITY : ExposifyEntityBase
{
    abstract val personalName: String
    var initialized: Boolean = false

    private val exceptionFn : (String, Int)-> OperationsException = { message, code ->
        OperationsException("$message @ $personalName", ExceptionCode.value(code))
    }

    protected val dtoList :  MutableList<CommonDTO<CHILD_DTO, DataModel, ExposifyEntityBase>> = mutableListOf()
    internal val childFactory: DTOFactory<CHILD_DTO, DataModel, ExposifyEntityBase>
        get() {
            return forClass.config.dtoFactory
        }

    protected suspend fun updateDtos(uninitializedDtos : List<ChildDTO<CHILD_DTO>>): List<ChildDTO<CHILD_DTO>>{
        uninitializedDtos.forEach {dto->
            dto.apply {
                crudOperation.setOperation(CrudType.UPDATE)
                dataContainer.trackedProperties.values.forEach { dataModelProperty ->
                    runCatching {
                        val childDataModels = dataContainer.extractChildModels(dataModelProperty)
                        repositories.map[dataModelProperty.getContainer().thisKey].getOrThrow("Child repository not found",
                            ExceptionCode.REPOSITORY_NOT_FOUND, exceptionFn).sharedUpdateByDataModels(childDataModels)
                    }.onFailure {
                        println(it.message.toString())
                        throw it
                    }
                }
            }
        }
        dtoList += uninitializedDtos.filterNot { it in dtoList }
        initialized = true
        return dtoList
    }

    protected suspend fun sharedUpdateByDataModels(dataModels : List<DataModel>):List<ChildDTO<CHILD_DTO>>{
       val createdDtos =  mutableListOf<CommonDTO<CHILD_DTO,  DataModel, ExposifyEntityBase>>()
        runCatching {
            dataModels.forEach {dataModel->
                childFactory.createDto(dataModel)?.let {
                    createdDtos.add(it)
                }
            }
        }.onFailure {
            println(it.message.toString())
            throw it
        }
       return updateDtos(createdDtos)
    }

    suspend fun selectDtos(dtoList: List<ChildDTO<CHILD_DTO>>): List<ChildDTO<CHILD_DTO>>{
        dtoList.forEach {dto->
            dto.apply {
                daoService.trackedProperties.values.forEach {entityModelProperty->
                    runCatching {
                        val bindingContainer = entityModelProperty.getContainer()
                        val childEntities = daoService.extractChildEntities(entityModelProperty)
                        repositories.map[bindingContainer.thisKey].getOrThrow(
                            "Child repository not found",
                            ExceptionCode.REPOSITORY_NOT_FOUND, exceptionFn
                        ).sharedSelectByEntities(childEntities)
                    }.onFailure {
                        println(it.message.toString())
                        throw it
                    }
                }
            }
        }
        initialized = true
        return dtoList
    }

    suspend fun sharedSelectByEntities(entities: List<ENTITY>): List<ChildDTO<CHILD_DTO>>{
        val createdDtos =  mutableListOf<CommonDTO<CHILD_DTO,  DataModel, ExposifyEntityBase>>()
        entities.forEach { entity ->
            forClass.withFactory { factory ->
                val newDto = factory.createDto(entity).getOrThrow(OperationsException("Dot creation by entity failed @ $personalName",
                    ExceptionCode.FACTORY_CREATE_FAILURE, CancelType.SKIP_SELF)
                )
                createdDtos.add(newDto)
            }
        }

        return selectDtos(createdDtos)
    }
}