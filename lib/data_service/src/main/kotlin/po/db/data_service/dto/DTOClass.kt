package po.db.data_service.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import po.db.data_service.binder.BindingKeyBase
import po.db.data_service.binder.ChildContainer
import po.db.data_service.binder.OrdinanceType
import po.db.data_service.binder.UpdateMode
import po.db.data_service.components.eventhandler.EventHandler
import po.db.data_service.components.eventhandler.RootEventHandler
import po.db.data_service.components.eventhandler.interfaces.CanNotify
import po.db.data_service.dto.components.DTOConfig
import po.db.data_service.dto.components.Factory
import po.db.data_service.dto.interfaces.DTOInstance
import po.db.data_service.dto.interfaces.DTOModel
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.CommonDTO
import po.db.data_service.models.EntityDTO
import kotlin.reflect.KClass


abstract class DTOClass<DATA, ENTITY>(
    val sourceClass: KClass<out EntityDTO<DATA, ENTITY>>
): DTOInstance, CanNotify  where DATA : DataModel, ENTITY : LongEntity{

    companion object{

        protected fun <DATA: DataModel,ENTITY: LongEntity> saveNew(
            dto : EntityDTO<DATA, ENTITY>,
            entityModel: LongEntityClass<ENTITY>,
            block: ((ENTITY)-> Unit)? = null
        ): ENTITY? {
            try {
              val newEntity = entityModel.new {
                  dto.update(this, UpdateMode.MODEL_TO_ENTNTY)
                  block?.invoke(this)
              }
              return newEntity
            }catch (ex: Exception){
                println(ex.message)
                return null
            }
        }
    }

    override val qualifiedName  = sourceClass.qualifiedName.toString()
    override val className  = sourceClass.simpleName.toString()
    var initialized: Boolean = false
    val conf = DTOConfig<DATA, ENTITY>(this)

    val factory = Factory(this, sourceClass)

    val entityModel: LongEntityClass<ENTITY>
        get(){
            return  conf.entityModel?: throw OperationsException(
                "Unable read daoModel property on $className",
                ExceptionCodes.LAZY_NOT_INITIALIZED)
        }

    val bindings = mutableMapOf<BindingKeyBase, ChildContainer<DATA, ENTITY, *, *>>()

    private val repository = mutableListOf<EntityDTO<DATA, ENTITY>>()
     
    val tempRepository : MutableList<EntityDTO<DATA,ENTITY>> = mutableListOf()

    override val eventHandler = RootEventHandler(className)

    protected abstract fun setup()

    fun nowTime(): LocalDateTime {
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }

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
        block: DTOConfig<DATA,ENTITY>.() -> Unit
    ) where  ENTITY : LongEntity, DATA : DataModel{
        factory.initializeBlueprints(DATA::class, ENTITY::class)
        conf.dataModelClass = DATA::class
        conf.entityClass = ENTITY::class
        conf.entityModel = entityModel
        conf.block()
    }

    fun <CHILD_DATA: DataModel, CHILF_ENTITY: LongEntity> execute(fn : (DTOClass<DATA, ENTITY>)-> Unit  ){
        fn.invoke(this)
    }

    /**
     * Initialize CommonDTO received from the ServiceContext
     * @input commonDTO:  CommonDTO<DATA>
     * @return EntityDTO<DATA,ENTITY> or null
     * */
    fun initDTO(commonDTO: CommonDTO<DATA>) : EntityDTO<DATA,ENTITY>? {
       val existentEntityDTO =  tempRepository.firstOrNull { it.id == commonDTO.id }
        if(existentEntityDTO == null){
           val dto =  commonDTO.copyAsEntityDTO(this)

        }else{
            TODO("Reinitialize and update if needed")
        }
        return null
    }

    fun initDTO(entityDTO: EntityDTO<DATA, ENTITY>) : EntityDTO<DATA,ENTITY>? {
        val existentEntityDTO = tempRepository.firstOrNull { it.id == entityDTO.id }
        if(existentEntityDTO == null){
            when(entityDTO.isUnsaved){
                true ->{
                    conf.relationBinder?.bindings()?.forEach {
                        if(it.type == OrdinanceType.ONE_TO_MANY){
                            entityDTO.injectedDataModel
                        }
                    }
                    val entity = saveNew(entityDTO,entityModel)
                }
                false ->{
                    TODO("Has some Id. Get from the DB corresponding entity")
                }
            }
        }else{
            TODO("Reinitialize and update if needed")
        }
        return null
    }

    /**
     * Create new CommonDTO entity from DataModel provided
     * @input dataModel: DataModel
     * @return CommonDTO
     * */
    fun <PARENT_DATA: DataModel, PARENT_ENTITY: LongEntity>create(
        dataModel: DATA,
        block: ((ENTITY)-> Unit)? = null ) : EntityDTO<DATA, ENTITY>? {
        if(initialized == false){
            throw OperationsException(
                "Calling create(dataModel.id=${dataModel.id}) on model uninitialized",
                ExceptionCodes.NOT_INITIALIZED)
        }

        factory.createEntityDto(dataModel)?.let {newDto->
            newDto.initialize(this)
            saveNew(newDto, entityModel, block)

            bindings.keys.forEach {key->
                when(key.ordinance){
                    OrdinanceType.ONE_TO_MANY -> {
                        bindings[key]!!.createWithParent(newDto)
                    }
                    else -> {}
                }
            }
            return newDto
        }
        return  null
    }

}
