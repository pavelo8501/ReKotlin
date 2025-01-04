package po.db.data_service.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import po.db.data_service.binder.OrdinanceType
import po.db.data_service.binder.UpdateMode
import po.db.data_service.dto.components.DTOConfig
import po.db.data_service.dto.components.Factory
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.CommonDTO
import po.db.data_service.models.EntityDTO
import kotlin.reflect.KClass


sealed interface HierarchyRoot : HierarchyBase {

}

sealed interface HierarchyMember : HierarchyBase {

}

interface HierarchyBase {
    val className : String
}



abstract class DTOClass<DATA, ENTITY>(val sourceClass: KClass<out EntityDTO<DATA, ENTITY>>): HierarchyRoot where DATA : DataModel, ENTITY : LongEntity{

    companion object{

        protected fun <DATA: DataModel,ENTITY: LongEntity> newEntity(dto : EntityDTO<DATA, ENTITY>, entityModel: LongEntityClass<ENTITY>): ENTITY? {
            try {
              val newEntity = entityModel.new {
                    dto.updateDTO(this, UpdateMode.MODEL_TO_ENTNTY)
                }
                return newEntity
            }catch (ex: Exception){
                println(ex.message)
                return null
            }
        }
    }

    private val qualifiedClassName  = sourceClass.qualifiedName.toString()
    override val className  = sourceClass.simpleName.toString()
    var initialized: Boolean = false
    val conf = DTOConfig(this)

    val factory = Factory(this, sourceClass)

    val entityModel: LongEntityClass<ENTITY>
        get(){
            return  conf.entityModel?: throw OperationsException("Unable read daoModel property on $className", ExceptionCodes.LAZY_NOT_INITIALIZED)
        }

    init {


    }

    private val commonDTOContainer = mutableListOf<CommonDTO<DATA>>()
     
    val tempRepository : MutableList<EntityDTO<DATA,ENTITY>> = mutableListOf()

    protected abstract fun setup()

    fun nowTime(): LocalDateTime {
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }

    fun getAssociatedTables():List<IdTable<Long>>{
       val result = mutableListOf<IdTable<Long>>()
       result.add(this.entityModel.table)
       result.addAll(listOf(this.entityModel.table))
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
           when(dto.isUnsaved){
               true ->{
                   newEntity(dto,entityModel)?.let {
                       conf.relationBinder.onBindings(dto){

                       }
                   }
                   val entity =  newEntity(dto,entityModel)
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


    fun initDTO(entityDTO: EntityDTO<DATA, ENTITY>) : EntityDTO<DATA,ENTITY>? {
        val existentEntityDTO =  tempRepository.firstOrNull { it.id == entityDTO.id }
        if(existentEntityDTO == null){
            when(entityDTO.isUnsaved){
                true ->{

                    conf.relationBinder.bindings().forEach {
                        if(it.type == OrdinanceType.ONE_TO_MANY){
                            entityDTO.injectedDataModel
                        }
                    }
                    val entity = newEntity(entityDTO,entityModel)
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
    fun create(dataModel: DATA) : EntityDTO<DATA, ENTITY>? {

        val entityDTO =  factory.createEntityDto(dataModel)
        if(entityDTO != null){
            entityDTO.initialize(this)

        }
        return  entityDTO
    }

    inline fun <reified DATA,  reified ENTITY>   DTOClass<DATA, ENTITY>.dtoSettings(
        entityModel: LongEntityClass<ENTITY>,
        block: DTOConfig<DATA,ENTITY>.() -> Unit
    ) where  ENTITY : LongEntity, DATA : DataModel{
        factory.initializeBlueprints(DATA::class, ENTITY::class)
        conf.dataModelClass = DATA::class
        conf.entityClass = ENTITY::class
        conf.entityModel = entityModel
        conf.block()
    }

}
