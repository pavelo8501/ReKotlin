package po.db.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import po.db.data_service.binder.BindingKeyBase
import po.db.data_service.binder.ChildContainer
import po.db.data_service.binder.OrdinanceType
import po.db.data_service.binder.UpdateMode
import po.db.data_service.components.eventhandler.RootEventHandler
import po.db.data_service.components.eventhandler.interfaces.CanNotify
import po.db.data_service.dto.components.DAOService
import po.db.data_service.dto.components.DTOConfig
import po.db.data_service.dto.components.Factory
import po.db.data_service.dto.interfaces.DTOInstance
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.CrudResult
import po.db.data_service.models.EntityDTO
import kotlin.reflect.KClass

abstract class DTOClass<DATA, ENTITY>(
    val sourceClass: KClass<out EntityDTO<DATA, ENTITY>>
): DTOInstance, CanNotify  where DATA : DataModel, ENTITY : LongEntity{

    override val qualifiedName  = sourceClass.qualifiedName.toString()
    override val className  = sourceClass.simpleName.toString()
    override val eventHandler = RootEventHandler(className)

    var initialized: Boolean = false
    val conf = DTOConfig<DATA, ENTITY>(this)

    val factory = Factory(this, sourceClass)

    val entityModel: LongEntityClass<ENTITY>
        get(){
            return  conf.entityModel?: throw OperationsException(
                "Unable read daoModel property on $className",
                ExceptionCodes.LAZY_NOT_INITIALIZED)
        }

    val daoService  =  DAOService<DATA, ENTITY>(this)

    val bindings = mutableMapOf<BindingKeyBase, ChildContainer<DATA, ENTITY, *, *>>()

    protected abstract fun setup()

    fun getAssociatedTables():List<IdTable<Long>>{
       val result = mutableListOf<IdTable<Long>>()
       result.add(this.entityModel.table)
       bindings.values.forEach {
           result.add(it.childModel.entityModel.table)
       }
       return result
    }

    fun initialization(
        beforeInit: ((DTOClass<DATA,ENTITY>) -> Unit)? = null,
        onDtoAfter: (DTOClass<DATA,ENTITY>.() -> Unit)?= null
    ) {
        beforeInit?.let {
            it(this)
        }
        setup()
        initialized = true
        onDtoAfter?.let {
            this.it()
        }
    }

    inline fun <reified DATA, reified ENTITY> DTOClass<DATA, ENTITY>.dtoSettings(
        entityModel: LongEntityClass<ENTITY>,
        block: DTOConfig<DATA,ENTITY>.() -> Unit) where ENTITY: LongEntity, DATA: DataModel{
        factory.initializeBlueprints(DATA::class, ENTITY::class)
        conf.dataModelClass = DATA::class
        conf.entityClass = ENTITY::class
        conf.entityModel = entityModel
        conf.block()
    }

    /**
     * Initializes a DTO (Data Transfer Object) for a given data model.
     * If the data model has an ID of 0, it creates a new
     * entity DTO, initializes it, and saves it using the DAO service.
     * If the data model has an existing ID, it loads the corresponding entity and initializes a DTO for it.
     *
     * @param dataModel The data model to initialize the DTO for.
     * @param block An optional lambda function to perform additional actions on the entity before saving.
     * @return The initialized DTO or null if the creation process fails.
     * @throws OperationsException if the model is not properly initialized.
     */
    fun <PARENT_DATA: DataModel, PARENT_ENTITY: LongEntity>initDTO(
        dataModel : DATA,
        block: ((ENTITY)-> Unit)? = null): EntityDTO<DATA, ENTITY>?{
        notify("Initializing DTO for dataModel: $dataModel with keys: ${bindings.keys}")
        val dto = if(dataModel.id == 0L){
            factory.createEntityDto(dataModel)?.let {newDto->
                newDto.initialize(this)
                daoService.saveNew(newDto, block)
                bindings.keys.forEach { bindingKey ->
                    bindings[bindingKey]?.createFromDataModel(newDto)
                }
                newDto
            }
        }else{
           val entity = daoService.selectWhere(dataModel.id)
           val existentDto = initDTO(entity)
           return existentDto
        }
        return dto
    }

    /**
     * Initializes a DTO for a given entity by creating, initializing, and updating it.
     *
     * @param entity The entity to initialize the DTO for.
     * @return The initialized DTO or null if the creation process fails.
     * @throws OperationsException if the model is not properly initialized.
     */
    fun initDTO(entity: ENTITY): EntityDTO<DATA, ENTITY>?{
        if(initialized == false){
            throw OperationsException(
                "Calling create(entity.id=${entity.id.value}) on model uninitialized",
                ExceptionCodes.NOT_INITIALIZED)
        }
        factory.createEntityDto()?.let {newDto->
            newDto.initialize(this)
            newDto.update(entity, UpdateMode.ENTITY_TO_MODEL)
            bindings.keys.forEach {bindingKey->
                bindings[bindingKey]!!.createFromEntity(newDto)
            }
            return newDto
        }
        return null
    }

    /**
     * Selects all entities from the database, initializes DTOs for them, and returns a result containing these DTOs.
     *
     * @return A [CrudResult] containing a list of initialized DTOs and associated events.
     */
    fun select(): CrudResult<DATA, ENTITY> {
       val repository = mutableListOf<EntityDTO<DATA, ENTITY>>()
       notify("select()"){
           val entities = daoService.selectAll()
           entities.forEach {
               val dto = initDTO(it)
               if(dto != null){
                   repository.add(dto)
               }else{
                   TODO("Action on creation failure")
               }
           }
       }
       return CrudResult(repository.toList(), eventHandler.getEvent())
    }

    /**
     * Selects all entities from the database, initializes DTOs for them, and returns a result containing these DTOs.
     *
     * @return A [CrudResult] containing a list of initialized DTOs and associated events.
     */
    fun <PARENT_DATA: DataModel, PARENT_ENTITY: LongEntity>update(dataModels: List<DATA>): CrudResult<DATA, ENTITY>{
        val resultDTOs = mutableListOf<EntityDTO<DATA, ENTITY>>()
        notify("create() count=${dataModels.count()}") {
            dataModels.forEach {dataModel->
                val dto = initDTO<PARENT_DATA, PARENT_ENTITY>(dataModel)
                if(dto!=null){
                    resultDTOs.add(dto)
                }
            }
        }
        return CrudResult(resultDTOs.toList(), eventHandler.getEvent())
    }

    /**
     * Deletes a given DTO along with its bindings.
     * If bindings involve one-to-many relationships, it deletes the children before deleting the parent DTO.
     *
     * @param dto The DTO to delete.
     */
    fun delete(dto : EntityDTO<DATA, ENTITY>){
        bindings.values.forEach{binding->
            when(binding.type){
                OrdinanceType.ONE_TO_MANY -> {
                    binding.deleteChildren(dto)
                }
                else -> {

                }
            }
        }
        daoService.delete(dto)
    }

    /**
     * Deletes a given data model by first finding and initializing its DTO, then deleting it along with its bindings.
     *
     * @param dataModel The data model to delete.
     * @return A [CrudResult] containing a list of successfully deleted DTOs and associated events.
     */
    fun delete(dataModel: DATA): CrudResult<DATA, ENTITY>{
        val resultDTOs = mutableListOf<EntityDTO<DATA, ENTITY>>()
        notify("delete(dataModel.id = ${dataModel.id})") {
           val entity = daoService.selectWhere(dataModel.id)
           val dto = initDTO(entity)
           if(dto != null){
               delete(dto)
           }
        }
        return CrudResult(resultDTOs.toList(), eventHandler.getEvent())
    }

}
