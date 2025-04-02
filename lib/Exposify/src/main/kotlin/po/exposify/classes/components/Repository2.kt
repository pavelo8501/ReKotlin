package po.exposify.classes.components

import po.exposify.binders.relationship.BindingContainer2
import po.exposify.binders.relationship.MultipleChildContainer2
import po.exposify.binders.relationship.SingleChildContainer2
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.enums.CrudType
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase


class SingleRepository2<DTO, DATA, ENTITY,  CHILD_DTO>(
    val parent : CommonDTO<DTO, DATA, ENTITY>,
    val childClass: DTOClass2<CHILD_DTO>,
    val bindingContainer : SingleChildContainer2<DTO, DATA, ENTITY, CHILD_DTO>
): RepositoryBase2<DTO, DATA, ENTITY, CHILD_DTO>(childClass, bindingContainer)
        where DTO: ModelDTO, CHILD_DTO: ModelDTO,  DATA: DataModel, ENTITY : ExposifyEntityBase  {

    override val personalName: String =  "Repository[${parent.registryItem.typeKeyCombined}/Single]"

    override suspend fun update(dataModels : List<DataModel>) {
        val dataModel = dataModels.firstOrNull()
        if(dataModel != null){
            childClass.config.initFactoryRoutines()
            val parentEntity = parent.daoService.entity
            val newDto = childFactory.createDto(dataModel)
            if (parentEntity != null && newDto != null) {
                newDto.daoService.saveWithParent(newDto) { entity ->
                    bindingContainer.foreignEntityProperty.set(entity, parentEntity)
                }
                newDto.apply {
                    crudOperation.setOperation(CrudType.UPDATE)
                    dataContainer.trackedProperties.values.forEach { dataModelProperty ->
                        runCatching {
                            val bindingContainer = dataModelProperty.getContainer()
                            when (bindingContainer) {
                                is SingleChildContainer2 -> {
                                    val childDataModels = dataContainer.extractChildModels(dataModelProperty)
                                    repositories.map[bindingContainer.thisKey]?.let { childRepository ->
                                        childRepository.update(childDataModels)
                                    }
                                }

                                is MultipleChildContainer2 -> {
                                    val childDataModels = dataContainer.extractChildModels(dataModelProperty)
                                    repositories.map[bindingContainer.thisKey]?.let { childRepository ->
                                        childRepository.update(childDataModels)
                                    }
                                }
                            }
                        }
                    }
                    crudOperation.setOperation(CrudType.NONE)
                }
            }
        }
    }
}

class MultipleRepository2<DTO, DATA, ENTITY, CHILD_DTO>(
    val parent : CommonDTO<DTO, DATA, ENTITY>,
    val childClass: DTOClass2<CHILD_DTO>,
    val bindingContainer : MultipleChildContainer2<DTO, DATA, ENTITY, CHILD_DTO>,
): RepositoryBase2<DTO, DATA, ENTITY, CHILD_DTO>(childClass, bindingContainer)
    where DTO: ModelDTO, CHILD_DTO: ModelDTO, DATA: DataModel, ENTITY : ExposifyEntityBase {

    override val personalName: String = "Repository[${parent.registryItem.typeKeyCombined}/Multiple]"

    override suspend fun update(dataModels : List<DataModel>) {
        dataModels.forEach {dataModel->
            childClass.config.initFactoryRoutines()
            val parentEntity = parent.daoService.entity
            val newDto = childFactory.createDto(dataModel)
            if (parentEntity != null && newDto != null) {
                newDto.daoService.saveWithParent(newDto) { entity ->
                    bindingContainer.foreignEntityProperty.set(entity, parentEntity)
                }
                newDto.apply {
                    crudOperation.setOperation(CrudType.UPDATE)
                    dataContainer.trackedProperties.values.forEach { dataModelProperty ->
                        runCatching {
                            val bindingContainer = dataModelProperty.getContainer()
                            when (bindingContainer) {
                                is SingleChildContainer2 -> {
                                    val childDataModels = dataContainer.extractChildModels(dataModelProperty)
                                    repositories.map[bindingContainer.thisKey]?.let { childRepository ->
                                        childRepository.update(childDataModels)
                                    }
                                }

                                is MultipleChildContainer2 -> {
                                    val childDataModels = dataContainer.extractChildModels(dataModelProperty)
                                    repositories.map[bindingContainer.thisKey]?.let { childRepository ->
                                        childRepository.update(childDataModels)
                                    }
                                }
                            }
                        }
                    }
                    crudOperation.setOperation(CrudType.NONE)
                }
            }
        }
    }
}

class RootRepository2<DTO, DATA, ENTITY, CHILD_DTO>(
    val dtoClass: DTOClass2<CHILD_DTO>, override val personalName: String,
): RepositoryBase2<DTO,DATA, ENTITY, CHILD_DTO>(dtoClass) where DTO : ModelDTO, CHILD_DTO : ModelDTO, DATA: DataModel, ENTITY : ExposifyEntityBase
{
    override suspend fun update(dataModels : List<DataModel>) {
        dtoList.forEach {dto->
            dto.apply {
                crudOperation.setOperation(CrudType.UPDATE)
                dataContainer.trackedProperties.values.forEach { dataModelProperty ->
                    runCatching {
                        val bindingContainer = dataModelProperty.getContainer()
                        when (bindingContainer) {
                            is SingleChildContainer2 -> {
                                val childDataModels = dataContainer.extractChildModels(dataModelProperty)
                                repositories.map[bindingContainer.thisKey]?.let { childRepository ->
                                    childRepository.update(childDataModels)
                                }
                            }

                            is MultipleChildContainer2 -> {
                                val childDataModels = dataContainer.extractChildModels(dataModelProperty)
                                repositories.map[bindingContainer.thisKey]?.let { childRepository ->
                                    childRepository.update(childDataModels)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class RepositoryBase2<DTO, DATA, ENTITY, CHILD_DTO>(
    private val forClass: DTOClass2<CHILD_DTO>,
    private val binding : BindingContainer2<DTO, DATA, ENTITY, CHILD_DTO>? = null
) where DTO : ModelDTO, CHILD_DTO: ModelDTO, DATA: DataModel, ENTITY : ExposifyEntityBase
{
    abstract val personalName: String
    var initialized: Boolean = false

    protected val dtoList :  MutableList<CommonDTO<CHILD_DTO, DataModel, ExposifyEntityBase>> = mutableListOf()

    internal val childFactory: DTOFactory<CHILD_DTO, DataModel, ExposifyEntityBase>
        get() {
            return forClass.config.dtoFactory
        }

    abstract suspend fun update(dataModels : List<DataModel>)
    suspend fun update(dtoList : CommonDTO<DTO, DATA, ENTITY>){

    }

    suspend fun select(dtoList: List<CommonDTO<CHILD_DTO, DataModel, ExposifyEntityBase>>){
        dtoList.forEach {dto->
            dto.apply {
                crudOperation.setOperation(CrudType.SELECT)
                daoService.trackedProperties.values.forEach {entityModelProperty->
                    val bindingContainer =  entityModelProperty.getContainer()
                    when (bindingContainer) {
                        is SingleChildContainer2 -> {
                            val childEntity = daoService.extractChildEntities(entityModelProperty)
                            repositories.map[bindingContainer.thisKey]?.let { childRepository ->
                                childRepository.select(childEntity)
                            }
                        }
                        is MultipleChildContainer2 -> {
                            val childEntities = daoService.extractChildEntities(entityModelProperty)
                            repositories.map[bindingContainer.thisKey]?.let { childRepository ->
                                childRepository.select(childEntities)
                            }
                        }
                    }
                }
            }
        }
    }
    @JvmName("selectByEntities")
    suspend fun select(entities: List<ENTITY>): List<CommonDTO<CHILD_DTO,  DataModel, ExposifyEntityBase>>{
        entities.forEach { entity->
            forClass.withFactory {factory->
               val newDto =  factory.createDto(entity)
                if(newDto != null){
                    dtoList.add(newDto)
                }
            }
        }
        select(dtoList)
        return dtoList
    }

}