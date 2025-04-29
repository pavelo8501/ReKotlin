package po.exposify.dto.components

import po.exposify.dto.components.relation_binder.MultipleChildContainer
import po.exposify.dto.components.relation_binder.SingleChildContainer
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.classes.DTOClass
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.InitException
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.getOrOperationsEx
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
                hostingDto.castOrThrow<CommonDTO<ModelDTO, DataModel, ExposifyEntityBase>, OperationsException>()
            }
        } else {
            newChildDto.daoService.update()
        }
        childDTOList.add(newChildDto)
        //addChild(newChildDto)
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
                    hostingDto.castOrThrow<CommonDTO<ModelDTO, DataModel, ExposifyEntityBase>, OperationsException>()
                }
            }else{
                newChildDto.daoService.update()
            }
          //  addChild(newChildDto)
           childDTOList.add(newChildDto)
            newChildDto.getDtoRepositories().forEach {repository->
                repository.update()
            }
        }
    }

   suspend fun selectMultiple(){
        val entity =  hostingDto.daoService.getLastEntity()
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
    private val cumulativeDTOList : MutableList<CommonDTO<CHILD_DTO, DataModel, ExposifyEntityBase>> = mutableListOf()

    suspend fun update(dataModels : List<DATA>): List<CommonDTO<DTO, DATA, ENTITY>>{
        val result = mutableListOf<CommonDTO<DTO, DATA, ENTITY>>()
        dataModels.forEach {dataModel->
           val dto =  updateSingle(dataModel)
           result.add(dto)
        }
        return result
    }

    suspend fun updateSingle(dataModel : DATA): CommonDTO<DTO, DATA, ENTITY>{
        val castedDto = dtoClass.config.dtoFactory.createDto(dataModel).run {
            castOrThrow<CommonDTO<DTO, DATA, ENTITY>, OperationsException>()
        }
        if(dataModel.id == 0L){
            castedDto.daoService.save()
        }else{
            castedDto.daoService.update()
        }
        castedDto.getDtoRepositories().forEach {repository->
            repository.update()
        }
        return castedDto
    }

    suspend fun select(entities: List<ENTITY>): List<CommonDTO<DTO, DATA, ENTITY>>{
        val result = mutableListOf<CommonDTO<DTO, DATA, ENTITY>>()
        entities.forEach { entity ->
           val dto = selectSingle(entity)
           result.add(dto)
        }
        return result
    }

    suspend fun selectSingle(entity: ENTITY):CommonDTO<DTO, DATA, ENTITY>{
        val castedDto =  dtoClass.config.dtoFactory.createDto().run {
            castOrThrow<CommonDTO<DTO, DATA, ENTITY>, OperationsException>()
        }
        val initializedDto =  castedDto.updateBinding(entity, UpdateMode.ENTITY_TO_MODEL)
        initializedDto.getDtoRepositories().forEach { repository ->
            repository.select()
        }
        return initializedDto
    }

    fun addDto(dto : CommonDTO<CHILD_DTO, DataModel, ExposifyEntityBase>){
        cumulativeDTOList.add(dto)
    }

    fun getDtos():List<CommonDTO<CHILD_DTO, DataModel, ExposifyEntityBase>>{
        return cumulativeDTOList
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

    protected fun addChild(dto: CommonDTO<CHILD_DTO, DataModel, ExposifyEntityBase>){
        childDTOList.add(dto)
        childClass.repository!!.addDto(dto)
    }

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

    fun findChild(
        id: Long,
        lookUpClass: DTOClass<CHILD_DTO>): CommonDTO<CHILD_DTO, DataModel, ExposifyEntityBase>?
    {
        if(childClass == lookUpClass ){
            throw InitException("Looking in wrong DTO Class", ExceptionCode.ABNORMAL_STATE)
        }

        return  childDTOList.firstOrNull{it.id == id}

    }

    abstract suspend fun clear():RepositoryBase<DTO, DATA, ENTITY, CHILD_DTO>

}