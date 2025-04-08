package po.exposify.dto.components

import po.exposify.binders.relationship.BindingContainer2
import po.exposify.binders.relationship.MultipleChildContainer2
import po.exposify.binders.relationship.SingleChildContainer2
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.classes.DTOClass
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.countEqualsOrWithThrow
import po.lognotify.TasksManaged
import po.lognotify.exceptions.enums.CancelType
import po.lognotify.extensions.countEqualsOrThrow
import po.lognotify.extensions.getOrThrow
import po.lognotify.extensions.getOrThrowDefault
import po.lognotify.extensions.resultOrDefault
import po.lognotify.extensions.subTask
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
): RepositoryBase<DTO,DATA, ENTITY, CHILD_DTO>(dtoClass), TasksManaged
        where DTO : ModelDTO, CHILD_DTO : ModelDTO, DATA: DataModel, ENTITY : ExposifyEntityBase
{
    override val personalName: String = "Repository[${dtoClass.registryItem.commonDTOKClass.simpleName}/Root]"

    suspend fun updateByDataModels(dataModels : List<DATA>): List<CommonDTO<CHILD_DTO, DATA, ENTITY>>{
          val result =  sharedUpdateByDataModels(dataModels)
          val castedList = result.filterIsInstance<CommonDTO<CHILD_DTO, DATA, ENTITY>>()
          castedList.countEqualsOrThrow<CommonDTO<CHILD_DTO, DATA, ENTITY>, OperationsException>(
              result.count(),
              "Cast to CommonDTO<CHILD_DTO, DATA, ENTITY")
          return castedList
    }

    suspend fun selectByByEntities(entities: List<ENTITY>): List<CommonDTO<CHILD_DTO,  DATA, ENTITY>>  {
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

    protected suspend fun updateDtos(uninitializedDtos : List<ChildDTO<CHILD_DTO>>): List<ChildDTO<CHILD_DTO>>
     = subTask("update_dtos_initialization", "Repository ${forClass.personalName}") {
        uninitializedDtos.forEach {dto->
            dto.apply {
                dataContainer.trackedProperties.values.forEach { dataModelProperty ->
                val childDataModels = dataContainer.extractChildModels(dataModelProperty)
                repositories.map[dataModelProperty.getContainer().thisKey].
                    getOrThrowDefault("Child repository not found").sharedUpdateByDataModels(childDataModels)
                }
            }
        }
        dtoList += uninitializedDtos.filterNot { it in dtoList }
        initialized = true
        dtoList
    }.resultOrException()

    protected suspend fun sharedUpdateByDataModels(dataModels : List<DataModel>):List<ChildDTO<CHILD_DTO>> =
       subTask("repository_update_by_data_model"){handler->
        val createdDtos =  mutableListOf<CommonDTO<CHILD_DTO,  DataModel, ExposifyEntityBase>>()
        handler.info("Creating DTOs")
        dataModels.forEach {dataModel->
            childFactory.createDto(dataModel)?.let {
                createdDtos.add(it)
            }
        }
       handler.info("Dtos created ${createdDtos.count()}")
       updateDtos(createdDtos)
    }.resultOrDefault(emptyList())

    suspend fun selectDtos(dtoList: List<ChildDTO<CHILD_DTO>>): List<ChildDTO<CHILD_DTO>>{
        dtoList.forEach {dto->
            dto.apply {
                daoService.trackedProperties.values.forEach {entityModelProperty->
                    val bindingContainer = entityModelProperty.getContainer()
                    val childEntities = daoService.extractChildEntities(entityModelProperty)
                    repositories.map[bindingContainer.thisKey].getOrThrow(OperationsException("Child repository not found",
                        ExceptionCode.REPOSITORY_NOT_FOUND)).sharedSelectByEntities(childEntities)
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