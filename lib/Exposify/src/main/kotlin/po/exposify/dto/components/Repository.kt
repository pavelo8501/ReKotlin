package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.components.relation_binder.MultipleChildContainer
import po.exposify.dto.components.relation_binder.SingleChildContainer
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOClass
import po.exposify.dto.components.proFErty_binder.containerize
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.relation_binder.BindingContainer
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CrudOperation
import po.exposify.dto.models.trackSave
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx
import po.exposify.extensions.testOrThrow
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import po.misc.types.getOrThrow
import kotlin.collections.forEach

class SingleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE>(
    val binding : SingleChildContainer<DTO, DATA, ENTITY, C_DTO,  CD, CE>,
    hostingDto : CommonDTO<DTO, DATA, ENTITY>,
    childClass: DTOClass<C_DTO, CD, CE>,
): RepositoryBase<DTO,DATA, ENTITY, C_DTO, CD, CE>(hostingDto,binding,  childClass), TasksManaged
        where DTO : ModelDTO, DATA: DataModel, ENTITY : LongEntity,
              C_DTO : ModelDTO, CD: DataModel, CE: LongEntity
{
    override val qualifiedName: String get() = "SingleRepository[${hostingDTO.dtoName}]"
    override val name: String  get() = "SingleRepository"

    fun getDTO(): CommonDTO<C_DTO, CD, CE>{
        return childDTO.values.firstOrNull()
            .getOrThrow<CommonDTO<C_DTO, CD, CE>, OperationsException>(
                "Unable to get dto in $qualifiedName",
                ExceptionCode.ABNORMAL_STATE.value)
    }


    override suspend fun update(dataModel:CD): CommonDTO<C_DTO, CD, CE>?{
        if(dataModel.id == 0L){
            val newChildDto = childFactory.createDto(dataModel)
            childDaoService.saveWithParent(newChildDto, hostingDTO) { containerized ->
                binding.attachForeignEntity(containerized)
            }
            childDTO[newChildDto.id] = newChildDto
            newChildDto.getDtoRepositories().forEach { repository ->
                repository.loadHierarchyByEntity()
            }
            return newChildDto
        }else {
            val existingDto = childDTO[dataModel.id]
            if(existingDto != null) {
                existingDto.dtoPropertyBinder.update(dataModel)
                return existingDto
            }else{
                return null
            }
        }
    }


    override suspend fun loadHierarchyByModel() {
        val dataModel = binding.getDataModel(hostingDTO.dataModel)
        val newChildDto = childFactory.createDto(dataModel)
        if (dataModel.id == 0L) {
            childDaoService.saveWithParent(newChildDto, hostingDTO){containerized->
                binding.attachForeignEntity(containerized)
            }
        } else {
            childDaoService.update(newChildDto)
        }
        childDTO[newChildDto.id] = newChildDto
        newChildDto.getDtoRepositories().forEach {repository->
            repository.loadHierarchyByModel()
        }
    }


    override suspend fun loadHierarchyByEntity() {

        val childEntity = binding.getChildEntity(hostingDTO.daoEntity)
        val newChildDto = childFactory.createDto()
        newChildDto.updateBindingsAfterInserted(childEntity.containerize(UpdateMode.ENTITY_TO_MODEL))

        binding.saveDataModel(newChildDto.dataModel)
        childDTO[newChildDto.id] = newChildDto
        newChildDto.getDtoRepositories().forEach {repository->
             repository.loadHierarchyByEntity()
       }
    }

    override suspend fun clear(): SingleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE>{
        childDTO.clear()
        return this
    }
}

class MultipleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE>(
    val binding : MultipleChildContainer<DTO, DATA, ENTITY, C_DTO,  CD, CE>,
    hostingDto : CommonDTO<DTO, DATA, ENTITY>,
    childClass: DTOClass<C_DTO, CD, CE>,
): RepositoryBase<DTO,DATA, ENTITY, C_DTO, CD, CE>(hostingDto,binding,  childClass), TasksManaged
        where DTO : ModelDTO, DATA: DataModel, ENTITY : LongEntity,
              C_DTO : ModelDTO, CD: DataModel, CE: LongEntity
{

    override val qualifiedName: String get() = "MultipleRepository[${hostingDTO.dtoName}]"
    override val name: String  get() = "MultipleRepository"


    override suspend fun update(dataModel:CD): CommonDTO<C_DTO, CD, CE>?{
        if(dataModel.id == 0L){
            val newChildDto = childFactory.createDto(dataModel)
            childDaoService.saveWithParent(newChildDto, hostingDTO) { containerized ->
                binding.attachForeignEntity(containerized)
            }
            childDTO[newChildDto.id] = newChildDto
            newChildDto.getDtoRepositories().forEach { repository ->
                repository.loadHierarchyByEntity()
            }
            return newChildDto
        }else {
            val existingDto = childDTO[dataModel.id]
            if(existingDto != null) {
                existingDto.dtoPropertyBinder.update(dataModel)
                return existingDto
            }else{
                return null
            }
        }
    }

    suspend fun update(dataModels: List<CD>): List<CommonDTO<C_DTO, CD, CE>> {
       val resultList : MutableList<CommonDTO<C_DTO, CD, CE>> = mutableListOf()
        dataModels.forEach {
           val dto = update(it)
            if(dto != null){
                resultList.add(dto)
            }
        }
        return resultList
    }


    override suspend fun loadHierarchyByModel(): Unit = subTask("Update", qualifiedName){ handler->

        val dataModels = binding.getDataModels(hostingDTO.dataModel)
        handler.info("Update for parent dto ${hostingDTO.dtoName} and id ${hostingDTO.id} ")
        handler.info("Data Models count :${dataModels.count()} received from property ${binding.delegateName}")
        dataModels.forEach {dataModel->
            val newChildDto =  childFactory.createDto(dataModel)
            if(dataModel.id == 0L){
                childDaoService.saveWithParent(newChildDto, hostingDTO){containerized->
                    binding.attachForeignEntity(containerized)
                }
            }else{
                childDaoService.update(newChildDto)
            }
            childDTO[newChildDto.id] = newChildDto
            newChildDto.getDtoRepositories().forEach {repository->
                repository.loadHierarchyByModel()
            }
        }
    }.resultOrException()

    override suspend fun loadHierarchyByEntity(){

        val dtos = binding.getForeignEntities(hostingDTO.daoEntity).map { entity ->
            childFactory.createDto().also { dto ->
                dto.dtoPropertyBinder.afterInsertUpdate(entity.containerize(UpdateMode.ENTITY_TO_MODEL))
                dto.getDtoRepositories().forEach { it.loadHierarchyByEntity() }
            }
        }
        binding.saveDataModels(dtos.map { it.dataModel })
        dtos.forEach {
            childDTO[it.id] = it
        }
    }

    fun getDTO(): List<CommonDTO<C_DTO, CD, CE>>{
        return childDTO.values.toList().testOrThrow(
            OperationsException(
                "Dto list empty in $qualifiedName",
                ExceptionCode.ABNORMAL_STATE)){
                it.count() != 0
        }
    }

    override suspend fun clear(): MultipleRepository<DTO, DATA, ENTITY, C_DTO, CD, CE>{
        childDTO.clear()
        return this
    }
}


sealed class RepositoryBase<DTO, DATA, ENTITY, C_DTO,  CD, CE>(
    val hostingDTO: CommonDTO<DTO, DATA, ENTITY>,
    val bindingBase : BindingContainer<DTO, DATA, ENTITY, C_DTO,  CD, CE>,
    val childClass: DTOClass<C_DTO, CD, CE>,
): IdentifiableComponent
        where DTO : ModelDTO, DATA: DataModel, ENTITY : LongEntity,
            C_DTO : ModelDTO, CD: DataModel, CE: LongEntity
{
    abstract override val qualifiedName: String
    abstract  override val name: String

    var initialized: Boolean = false

    protected val childDTO: MutableMap<Long, CommonDTO<C_DTO, CD, CE>> = mutableMapOf()

    internal val childFactory: DTOFactory<C_DTO, CD, CE>
        get() { return childClass.config.dtoFactory }

    internal val childDaoService: DAOService<C_DTO, CD, CE>
        get() { return childClass.config.daoService }

    suspend fun pickById(id: Long): CommonDTO<C_DTO, CD, CE>{
        val found = childDTO[id]
        return  if(found != null){
            found
        }else{
            val childEntity = bindingBase.getForeignEntity(id).getOrOperationsEx<CE>("Foreign entity with id ${id} not found")
            val newChildDto = childFactory.createDto()
            newChildDto.dtoPropertyBinder.update(childEntity.containerize(UpdateMode.ENTITY_TO_MODEL))
            childDTO[newChildDto.id] = newChildDto
            newChildDto.getDtoRepositories().forEach {repository->
                repository.loadHierarchyByEntity()
            }
            newChildDto
        }
    }

    suspend fun pick(conditions: Query): CommonDTO<C_DTO, CD, CE>{
       val result =  childClass.config.daoService.pick(conditions)
           .getOrOperationsEx("Foreign entity not found for given query:${conditions}")
        val found = childDTO[result.id.value]
        return found ?: pickById(result.id.value)
    }

    suspend fun select(conditions: Query): List<CommonDTO<C_DTO, CD, CE>>{
        val resultingList = mutableListOf<CommonDTO<C_DTO, CD, CE>>()
        val result =  childClass.config.daoService.select(conditions)
        result.forEach { selectedEntity->
            val existingDto = childDTO[selectedEntity.id.value]
            if(existingDto != null){
                resultingList.add(existingDto)
            }else{
                val newDto =  pickById(selectedEntity.id.value)
                resultingList.add(newDto)
            }
        }
        return resultingList
    }


   abstract suspend fun  update(dataModel:CD): CommonDTO<C_DTO, CD, CE>?


//    suspend fun update(){
//        when(this){
//            is MultipleRepository-> {
//                hostingDTO.trackSave(CrudOperation.Update, this).let {
//                    updateMultiple()
//                    it.addTrackInfoResult(childDTO.count())
//                }
//            }
//            is SingleRepository-> {
//                hostingDTO.trackSave(CrudOperation.Update, this).let {
//                    updateSingle()
//                    it.addTrackInfoResult(childDTO.count())
//                }
//            }
//        }
//    }

    internal abstract suspend fun  loadHierarchyByEntity()
    internal abstract suspend fun  loadHierarchyByModel()
//    internal suspend fun loadHierarchyByEntity(){
//        when(this){
//            is MultipleRepository->{
//                hostingDTO.trackSave(CrudOperation.Update, this).let {
//                    loadHierarchyMultiple()
//                    it.addTrackInfoResult(childDTO.count())
//                }
//            }
//            is SingleRepository->{
//                hostingDTO.trackSave(CrudOperation.Update, this).let {
//                    loadHierarchySingle()
//                    it.addTrackInfoResult(childDTO.count())
//                }
//            }
//        }
//        initialized = true
//    }
    abstract suspend fun clear():RepositoryBase<DTO, DATA, ENTITY, C_DTO,  CD, CE>

}