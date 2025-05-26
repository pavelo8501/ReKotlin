package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.components.relation_binder.components.MultipleChildContainer
import po.exposify.dto.components.relation_binder.components.SingleChildContainer
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOClass
import po.exposify.dto.components.proFErty_binder.containerize
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.relation_binder.components.BindingContainer
import po.exposify.dto.components.relation_binder.components.BindingContainerAdv
import po.exposify.dto.components.relation_binder.components.MultipleChildContainerAdv
import po.exposify.dto.components.relation_binder.components.SingleChildContainerAdv
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.components.result.createResultList
import po.exposify.dto.components.result.createSingleResult
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.extensions.addTrackerInfo
import po.exposify.dto.components.tracker.extensions.addTrackerResult
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.ExecutionContext
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.getOrOperationsEx
import po.exposify.extensions.testOrThrow
import po.lognotify.TasksManaged
import po.lognotify.classes.task.TaskHandler
import po.lognotify.extensions.subTask
import po.lognotify.lastTaskHandler
import kotlin.collections.forEach

class SingleRepositoryAdv<DTO, DATA, ENTITY, C_DTO, CD, CE>(
    val binding : SingleChildContainerAdv<DTO, DATA, ENTITY, C_DTO,  CD, CE>,
    hostingDto : CommonDTO<DTO, DATA, ENTITY>,
): RepositoryBaseAdv<DTO,DATA, ENTITY, C_DTO, CD, CE>(hostingDto,  binding)
        where DTO : ModelDTO, DATA: DataModel, ENTITY : LongEntity,
              C_DTO : ModelDTO, CD: DataModel, CE: LongEntity
{
    override val qualifiedName: String get() = "SingleRepository[${hostingDTO.dtoName}]"
    override val type: ComponentType = ComponentType.SingleRepository

    internal var childDTO :  CommonDTO<C_DTO, CD, CE>? = null

    fun getDTO(): CommonDTO<C_DTO, CD, CE> {
        return childDTO.getOrOperationsEx()
    }

    override suspend fun loadHierarchyByModel() {
        val dataModel = binding.getDataModel(hostingDTO.dataModel)
        val newChildDto = childFactory.createDto(dataModel)
        if (dataModel.id == 0L) {
            newChildDto.addTrackerInfo(CrudOperation.Insert, this)
            childDaoService.saveWithParent(newChildDto, hostingDTO){containerized->
                binding.attachForeignEntity(containerized)
            }
        } else {
            newChildDto.addTrackerInfo(CrudOperation.Update, this)
            childDaoService.update(newChildDto)
        }
        store(newChildDto)
        newChildDto.getDtoRepositories().forEach {repository->
            repository.loadHierarchyByModel()
        }
        newChildDto.addTrackerResult()
    }

    override suspend fun loadHierarchyByEntity() {
        binding.getForeignEntity(hostingDTO.daoEntity).let { entity->
            childFactory.createDto().also { dto ->
                dto.dtoPropertyBinder.update(entity.containerize(UpdateMode.ENTITY_TO_MODEL, null, true))
                store(dto)
                dto.getDtoRepositories().forEach { it.loadHierarchyByEntity() }
            }
        }
    }

    override suspend fun clear(): SingleRepositoryAdv<DTO, DATA, ENTITY, C_DTO, CD, CE>{
        childDTO = null
        return this
    }
}

class MultipleRepositoryAdv<DTO, DATA, ENTITY, C_DTO, CD, CE>(
    val binding : MultipleChildContainerAdv<DTO, DATA, ENTITY, C_DTO,  CD, CE>,
    hostingDto : CommonDTO<DTO, DATA, ENTITY>
): RepositoryBaseAdv<DTO,DATA, ENTITY, C_DTO, CD, CE>(hostingDto,binding)
        where DTO : ModelDTO, DATA: DataModel, ENTITY : LongEntity,
              C_DTO : ModelDTO, CD: DataModel, CE: LongEntity
{



    override val qualifiedName: String get() = "MultipleRepository[${hostingDTO.dtoName}]"
    override val type: ComponentType = ComponentType.MultipleRepository

    internal val childDTO: MutableMap<Long,  CommonDTO<C_DTO, CD, CE>> = mutableMapOf()

    override suspend fun loadHierarchyByModel(): Unit
    = subTask("Update"){ handler->
        val dataModels = binding.getDataModels(hostingDTO.dataModel)
        handler.info("Update for parent dto ${hostingDTO.dtoName} and id ${hostingDTO.id} ")
        handler.info("Data Models count :${dataModels.count()} received from property ${binding.delegateName}")
        dataModels.forEach {dataModel->
            val newChildDto = childFactory.createDto(dataModel)
            if(dataModel.id == 0L){
                newChildDto.addTrackerInfo(CrudOperation.Insert, this)
                childDaoService.saveWithParent(newChildDto, hostingDTO){containerized->
                    binding.attachToForeignEntity(containerized)
                }
            }else{
                newChildDto.addTrackerInfo(CrudOperation.Update, this)
                childDaoService.update(newChildDto)
            }
            store(newChildDto)
            newChildDto.getDtoRepositories().forEach {repository->
                repository.loadHierarchyByModel()
            }
            newChildDto.addTrackerResult()
        }
    }.resultOrException()

    override suspend fun loadHierarchyByEntity(){
        binding.getForeignEntities(hostingDTO.daoEntity).map { entity ->
            childFactory.createDto().also { dto ->
                binding.saveDataModel(dto.dataModel)
                dto.dtoPropertyBinder.update(entity.containerize(UpdateMode.ENTITY_TO_MODEL, null, true))
                dto.dtoClass.config.dtoFactory.createDataModel()

                store(dto)
                dto.getDtoRepositories().forEach { it.loadHierarchyByEntity() }
            }
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

    override suspend fun clear(): MultipleRepositoryAdv<DTO, DATA, ENTITY, C_DTO, CD, CE>{
        childDTO.clear()
        return this
    }
}


sealed class RepositoryBaseAdv<DTO, DATA, ENTITY, C_DTO,  CD, CE>(
    val hostingDTO: CommonDTO<DTO, DATA, ENTITY>,
    val bindingBase : BindingContainerAdv<DTO, DATA, ENTITY, C_DTO,  CD, CE>,
): ExecutionContext<C_DTO, CD, CE>,  IdentifiableComponent, TasksManaged
        where DTO : ModelDTO, DATA: DataModel, ENTITY : LongEntity,
            C_DTO : ModelDTO, CD: DataModel, CE: LongEntity
{

    protected val childClass: DTOClass<C_DTO, CD, CE> get() = bindingBase.childClass

    abstract override val qualifiedName: String
    override val dtoClass : DTOClass<C_DTO, CD, CE> get() = childClass
    override val logger : TaskHandler<*> get() = lastTaskHandler()

    var initialized: Boolean = false

    internal val childFactory: DTOFactory<C_DTO, CD, CE>
        get() {
            return childClass.config.dtoFactory
        }

    internal val childDaoService: DAOService<C_DTO, CD, CE>
        get() {
            return childClass.config.daoService
        }

    fun store(dto : CommonDTO<C_DTO, CD, CE>): CommonDTO<C_DTO, CD, CE>{
        when(this){
            is SingleRepositoryAdv -> { childDTO = dto }
            is MultipleRepositoryAdv->{ childDTO[dto.id] = dto }
        }
        childClass.registerDTO(dto)
        return dto
    }

    fun takeStored(id: Long): CommonDTO<C_DTO, CD, CE>?{
       val dto =  childClass.lookupDTO(id)
       return dto
    }

    private suspend fun createDto(entity: CE):CommonDTO<C_DTO, CD, CE>{
        val dto = childClass.config.dtoFactory.createDto()
        dto.dtoPropertyBinder.update(entity.containerize(UpdateMode.ENTITY_TO_MODEL, null, true))
        store(dto)
        dto.getDtoRepositories().forEach { it.loadHierarchyByEntity() }
        return dto
    }

    private suspend fun insert(dataModel:CD): ResultSingle<C_DTO, CD, CE> {
        val newChildDto = childClass.config.dtoFactory.createDto(dataModel)
        newChildDto.addTrackerInfo(CrudOperation.Insert, this)
        childClass.config.daoService.saveWithParent(newChildDto, hostingDTO) { containerized ->
            when (this) {
                is SingleRepositoryAdv -> binding.attachForeignEntity(containerized)
                is MultipleRepositoryAdv -> binding.attachToForeignEntity(containerized)
            }
        }
        store(newChildDto)
        newChildDto.getDtoRepositories().forEach { it.loadHierarchyByModel() }
        with(childClass) {
            return newChildDto.createSingleResult(CrudOperation.Insert)
        }
    }

    override suspend fun pickById(id: Long): ResultSingle<C_DTO, CD, CE>{
        val existent = childClass.lookupDTO(id)
        return if(existent != null){
            existent.addTrackerInfo(CrudOperation.Pick, this)
            with(childClass){ existent.createSingleResult(CrudOperation.Pick) }
        }else{
            val entity = childClass.config.daoService.pickById(id).getOrOperationsEx("Entity with provided id :${id} not found")
            with(childClass){ createDto(entity).createSingleResult(CrudOperation.Pick) }
        }
    }

    override suspend fun pick(conditions: SimpleQuery): ResultSingle<C_DTO, CD, CE> {
        val entity = childClass.config.daoService.pick(conditions).getOrOperationsEx("Entity with provided query :${conditions} not found")
        val existent = childClass.lookupDTO(entity.id.value)
        return if(existent != null){
            existent.addTrackerInfo(CrudOperation.Pick, this)
            with(childClass){  existent.createSingleResult(CrudOperation.Pick) }
        }else{
            with(childClass){ createDto(entity).createSingleResult(CrudOperation.Pick)}
        }
    }

    override suspend fun <T : IdTable<Long>> select(
        conditions: WhereQuery<T>
    ): ResultList<C_DTO, CD, CE> = select(conditions)

    override suspend fun select(): ResultList<C_DTO, CD, CE>{
        val dtos = mutableListOf<CommonDTO<C_DTO, CD, CE>>()
        val result = childClass.config.daoService.select()
        result.forEach { selectedEntity ->
            val existingDto = takeStored(selectedEntity.id.value)
            if (existingDto != null) {
                dtos.add(existingDto)
            } else {
                val newDto = pickById(selectedEntity.id.value)
                dtos.add(newDto.getAsCommonDTOForced())
            }
        }
        return dtos.createResultList(childClass, CrudOperation.Select)
    }

    override suspend fun select(conditions: SimpleQuery): ResultList<C_DTO, CD, CE> {
        val dtos = mutableListOf<CommonDTO<C_DTO, CD, CE>>()
        val result = childClass.config.daoService.select(conditions)
        result.forEach { selectedEntity ->
            val existingDto = takeStored(selectedEntity.id.value)
            if (existingDto != null) {

                dtos.add(existingDto)
            } else {
                val newDto = pickById(selectedEntity.id.value)
                dtos.add(newDto.getAsCommonDTOForced())
            }
        }
        return dtos.createResultList(childClass, CrudOperation.Select)
    }
    override suspend fun update(dataModel:CD): CommonDTO<C_DTO, CD, CE>{
        if(dataModel.id == 0L){
            return insert(dataModel).getAsCommonDTOForced()
        }else {
            val existingDto = takeStored(dataModel.id)
            if(existingDto != null){
                existingDto.dtoPropertyBinder.update(dataModel)
                return existingDto
            }else{
                val pickedDTO =  pickById(dataModel.id)
                return pickedDTO.getAsCommonDTOForced()
            }
        }
    }

    override suspend fun update(dataModels: List<CD>): ResultList<C_DTO, CD, CE>{
        val dtos = mutableListOf<CommonDTO<C_DTO, CD, CE>>()
        dataModels.forEach {
            dtos.add(update(it))
        }
        return dtos.createResultList(childClass, CrudOperation.Update)
    }

    internal abstract suspend fun loadHierarchyByEntity()
    internal abstract suspend fun loadHierarchyByModel()

    abstract suspend fun clear(): RepositoryBaseAdv<DTO, DATA, ENTITY, C_DTO, CD, CE>

}