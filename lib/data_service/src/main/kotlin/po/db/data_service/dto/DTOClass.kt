package po.db.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.binder.BindingKeyBase
import po.db.data_service.binder.ChildContainer
import po.db.data_service.binder.OrdinanceType
import po.db.data_service.binder.UpdateMode
import po.db.data_service.components.eventhandler.RootEventHandler
import po.db.data_service.components.eventhandler.interfaces.CanNotify
import po.db.data_service.dto.components.DAOService
import po.db.data_service.dto.components.DTOConfig
import po.db.data_service.dto.components.Factory
import po.db.data_service.dto.interfaces.DTOEntity
import po.db.data_service.dto.interfaces.DTOInstance
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.CrudResult
import po.db.data_service.models.CrudResultSingle
import po.db.data_service.models.EntityDTO
import kotlin.reflect.KClass

abstract class DTOClass<DATA, ENTITY>(
    val sourceClass: KClass<out EntityDTO<DATA, ENTITY>>
): DTOInstance, CanNotify  where DATA : DataModel, ENTITY : LongEntity{


//    companion object{
//
//        protected fun <DATA: DataModel,ENTITY: LongEntity> saveNew(
//            dto : EntityDTO<DATA, ENTITY>,
//            entityModel: LongEntityClass<ENTITY>,
//            block: ((ENTITY)-> Unit)? = null
//        ): ENTITY? {
//            try {
//              val newEntity = entityModel.new {
//                  dto.update(this, UpdateMode.MODEL_TO_ENTNTY)
//                  block?.invoke(this)
//              }
//              return newEntity
//            }catch (ex: Exception){
//                println(ex.message)
//                return null
//            }
//        }
//
//        protected fun <DATA: DataModel,ENTITY: LongEntity>updateExistent(
//            dto : EntityDTO<DATA, ENTITY>,
//            entityModel: LongEntityClass<ENTITY>
//        ){
//            try {
//                val entity = selectWhere(dto.id, entityModel)
//                dto.update(entity, UpdateMode.MODEL_TO_ENTNTY)
//            }catch (ex: Exception){
//                println(ex.message)
//            }
//        }
//
//        protected fun <ENTITY: LongEntity> selectAll(
//            entityModel: LongEntityClass<ENTITY>
//        ): SizedIterable<ENTITY>{
//            try {
//                return entityModel.all()
//            }catch (ex: Exception){
//                println(ex.message)
//                throw ex
//            }
//        }
//
//        protected fun <ENTITY: LongEntity> selectWhere(
//            id: Long,  entityModel: LongEntityClass<ENTITY>
//        ): ENTITY{
//            if(id == 0L) throw OperationsException("Id should be greater than 0", ExceptionCodes.INVALID_DATA)
//            val entity = entityModel[id]
//            return entity
//        }
//    }

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

    val daoService  = object : DAOService<DATA, ENTITY>(this){}

    val bindings = mutableMapOf<BindingKeyBase, ChildContainer<DATA, ENTITY, *, *>>()

    protected abstract fun setup()

    fun getAssociatedTables():List<IdTable<Long>>{
       val result = mutableListOf<IdTable<Long>>()
       result.add(this.entityModel.table)
       bindings.values.forEach {
           result.add(it.childDTOModel.entityModel.table)
       }
       return result
    }

    fun beforeInit(context : DTOClass<DATA,ENTITY>,  onDtoBefore: DTOClass<DATA,ENTITY>.() -> Unit){
        context.onDtoBefore()
    }

    fun afterInit( context : DTOClass<DATA,ENTITY>,  onDtoAfter: DTOClass<DATA,ENTITY>.() -> Unit){
         context.onDtoAfter()
    }

    fun initialization() {
        beforeInit(this) {
            println("${this::class.simpleName}  Class  before initialization")
        }
        setup()
        initialized = true
        afterInit(this) {
               println("${this::class.simpleName} Class initialization complete with result ${initialized}")
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


    fun <PARENT_DATA: DataModel, PARENT_ENTITY: LongEntity>initDTO(
        dataModel : DATA,
        block: ((ENTITY)-> Unit)? = null): EntityDTO<DATA, ENTITY>{
        val keys = bindings.keys
        val dto = if(dataModel.id == 0L){
            factory.createEntityDto(dataModel)?.let {newDto->
                keys.forEach {bindingKey->
                   bindings[bindingKey]!!.createFromDataModel(newDto)
                   if(block!= null){
                       daoService.saveNew(newDto, block)
                   }
                   newDto
                }
            }
            throw OperationsException("Factory Failed to create EntityDTO", ExceptionCodes.REFLECTION_ERROR)
        }else{
            select(dataModel.id)
        }
        return dto
    }

    /**
     * Create List of EntityDTO from the database records of source LongEntityClass<LongEntity>
     * @input entity: LongEntity
     * @return EntityDTO
     **/
    fun select(): CrudResult<DATA, ENTITY> {
       val entities = daoService.selectAll(entityModel)
       val repository = mutableListOf<EntityDTO<DATA, ENTITY>>()
       notify("select() count=${entities.count()}"){
           entities.forEach {
               val dto = create(it)
               if(dto != null){
                   repository.add(dto)
               }else{
                   TODO("Action on creation failure")
               }
           }
       }
       return CrudResult(repository.toList(), eventHandler.getEvent())
    }

    fun select(id: Long): EntityDTO<DATA, ENTITY>{
        val entity =  daoService.selectWhere(id, entityModel)
        val dto = create(entity)
        //!!! Not to forget implement return checks and exception handling
        return dto!!
    }

    /**
     * Create new EntityDTO from LongEntity provided
     * @input entity: LongEntity
     * @return EntityDTO
     **/
    fun create(
        entity: ENTITY
    ): EntityDTO<DATA, ENTITY>?{
        if(initialized == false){
            throw OperationsException(
                "Calling create(entity.id=${entity.id.value}) on model uninitialized",
                ExceptionCodes.NOT_INITIALIZED)
        }
        factory.createEntityDto()?.let {newDto->
            newDto.initialize(this)
            newDto.update(entity, UpdateMode.ENTITY_TO_MODEL)
            bindings.keys.forEach {key->
                when(key.ordinance){
                    OrdinanceType.ONE_TO_MANY -> {
                       bindings[key]!!.createFromEntity(newDto, key)
                    }
                    else -> {}
                }
            }
            return newDto
        }
        return null
    }

    /**
     * Create new EntityDTO from DataModel provided
     * @input dataModel: DataModel
     * @return EntityDTO
     **/
    fun <PARENT_DATA: DataModel, PARENT_ENTITY: LongEntity>create(
        dataModel: DATA,
        block: ((ENTITY)-> Unit)? = null): CrudResultSingle<DATA, ENTITY>?{

        if(initialized == false){
            throw OperationsException(
                "Calling create(dataModel.id=${dataModel.id}) on model uninitialized",
                ExceptionCodes.NOT_INITIALIZED)
        }

        factory.createEntityDto(dataModel)?.let {newDto->
            newDto.initialize(this)
            if(dataModel.id == 0L) {
                daoService.saveNew(newDto, block)
            }else{
                daoService.updateExistent(newDto,entityModel)
            }

            bindings.keys.forEach {key->
                when(key.ordinance){
                    OrdinanceType.ONE_TO_MANY -> {
                        bindings[key]!!.createFromDataModel(newDto)
                    }
                    else -> {}
                }
            }

            CrudResultSingle(newDto, eventHandler.getEvent())
            return CrudResultSingle(newDto, eventHandler.getEvent())
        }
        TODO("Substitute with fallback logic in order to return non nullable result")
        return null
    }

    /**
     * Create method variance for bulk creation from DataModel list
     * @input dataModels list of DataModel objects
     * @return CrudResult
     **/
    fun <PARENT_DATA: DataModel, PARENT_ENTITY: LongEntity>create(dataModels: List<DATA>): CrudResult<DATA, ENTITY>{
        val resultDTOs = mutableListOf<EntityDTO<DATA, ENTITY>>()
        notify("create() count=${dataModels.count()}") {
            dataModels.forEach {dataModel->
                val dto = initDTO<PARENT_DATA, PARENT_ENTITY>(dataModel)
                resultDTOs.add(dto)
            }
        }
        return CrudResult(resultDTOs.toList(), eventHandler.getEvent())
    }
}
