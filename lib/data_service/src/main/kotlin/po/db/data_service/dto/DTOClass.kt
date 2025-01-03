package po.db.data_service.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.binder.ChildContainer
import po.db.data_service.binder.OrdinanceType
import po.db.data_service.binder.RelationshipBinder
import po.db.data_service.constructors.ClassBlueprintContainer
import po.db.data_service.constructors.ConstructorBuilder
import po.db.data_service.dto.components.DTOConfig
import po.db.data_service.dto.interfaces.DTOEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.CommonDTO
import po.db.data_service.scope.service.models.DaoFactory
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

abstract class DTOClass<DATA, ENTITY> where DATA : DataModel, ENTITY : LongEntity{

    companion object : ConstructorBuilder()

    var initialized: Boolean = false
    var className : String = "Undefined"
    var conf = DTOConfig<DATA,ENTITY>(this,RelationshipBinder<ENTITY,DATA>(this))


    init {

    }

    var onDtoInitializationCallback: ((DTOClass<DATA, ENTITY>) -> ClassBlueprintContainer)? = null
        private set
    private var _blueprints : ClassBlueprintContainer? = null
    private val blueprints : ClassBlueprintContainer
        get(){
            return  _blueprints?: throw InitializationException("blueprints for Class $className no set", ExceptionCodes.LAZY_NOT_INITIALIZED)
        }

    val daoModel: LongEntityClass<ENTITY>
        get(){
            return  conf.daoModel?: throw OperationsException("Unable read daoModel property on $className", ExceptionCodes.LAZY_NOT_INITIALIZED)
        }

    private val dtoContainer = mutableListOf<CommonDTO<DATA>>()
     
    init {
        val a = this
    }

    protected abstract fun setup()

    fun nowTime(): LocalDateTime {
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }

    fun getAssociatedTables():List<IdTable<Long>>{
       val result = mutableListOf<IdTable<Long>>()
       result.add(this.daoModel.table)
       result.addAll(listOf(this.daoModel.table))
       return result
    }

    fun setConfiguration(className: String, config: DTOConfig<DATA,ENTITY>) {
        conf = config
        this.className = className
    }
    
    fun beforeInit(context : DTOClass<DATA,ENTITY>,  onDtoBefore: DTOClass<DATA,ENTITY>.() -> Unit){
        context.onDtoBefore()
    }

    fun afterInit( context : DTOClass<DATA,ENTITY>,  onDtoAfter: DTOClass<DATA,ENTITY>.() -> Unit){
         context.onDtoAfter()
    }

    fun initialization(onDtoInitialization: (DTOClass<DATA,ENTITY>) -> ClassBlueprintContainer) {
        beforeInit(this) {
            println("${this::class.simpleName}  Class  before initialization")
        }
        setup()
        onDtoInitialization(this).let {
            _blueprints = it
        }
        initialized = true
        afterInit(this) {
           if(this.initialized){

               println("${this::class.simpleName} Class initialization complete with result ${initialized}")
           }
        }
    }

    private fun constructDtoEntity(dataModel : DataModel):CommonDTO<DATA>{
        val dtoEntity = try {
            val params = blueprints.dtoModel.constructorParams
            val args = getArgsForConstructor(blueprints.dtoModel) {
                when (it) {
                    "dataModel" -> {
                        dataModel
                    }
                    else -> {
                        null
                    }
                }
            }
            val dtoEntity = blueprints.dtoModel.getEffectiveConstructor().callBy(args)
            return dtoEntity as CommonDTO<DATA>
        } catch (ex: Exception) {
            throw OperationsException("DTO entity creation failed ${ex.message} ", ExceptionCodes.REFLECTION_ERROR)
        }
        return dtoEntity
    }

    private fun constructDataModel():DataModel{
        try{
            val model = conf.dataModelConstructor?.invoke().let { model ->
                val args = getArgsForConstructor(blueprints.dataModel)
                model ?: blueprints.dataModel.getEffectiveConstructor().callBy(args) as DataModel
            }
            return model
        } catch (ex: Exception) {
            throw OperationsException("DataModel  creation failed ${ex.message}", ExceptionCodes.REFLECTION_ERROR)
        }
    }

    /**
     * Create new CommonDTO entity from DataModel provided
     * @input dataModel: DataModel
     * @return CommonDTO
     * */
    fun create(dataModel: DATA, daoFactory: DaoFactory) : CommonDTO<DATA> {
        val newDTO = create(dataModel)
        daoFactory.new(this)
        conf.relationBinder.getBindingList().forEach { binding->
            newDTO.childDataSource?.forEach { dataModel->
                val childDTO = binding.createChild(newDTO, dataModel, daoFactory)
                newDTO.childDTOs.add(childDTO)
            }
        }
        return  newDTO
    }

    fun create(dataModel: DATA) : CommonDTO<DATA> {
        val newDTO = constructDtoEntity(dataModel)
        dtoContainer.add(newDTO)
        return  newDTO
    }

    fun create(daoEntity: ENTITY) : CommonDTO<DATA> {
        val dataModel = constructDataModel()
        val newDTO = constructDtoEntity(dataModel)
        newDTO.updateDTO(daoEntity, this)
        conf.relationBinder.getBindingList().forEach { binding ->
            binding.loadChild(daoEntity).let {
                newDTO.childDTOs.addAll(it)
            }
        }
        dtoContainer.add(newDTO)
        return newDTO
    }

    inline fun <reified DATA, ENTITY>   DTOClass<DATA, ENTITY>.dtoSettings(
        daoModel: LongEntityClass<ENTITY>,
        block: DTOConfig<DATA,ENTITY>.() -> Unit
    ) where  ENTITY : LongEntity, DATA : DataModel {
        val rootDtoModelClass = DATA::class
    val newConf = DTOConfig<DATA,ENTITY>(this, RelationshipBinder<ENTITY,DATA >(this) )
        newConf.setClassData(rootDtoModelClass as KClass<out CommonDTO<DATA>>,DataModel::class, daoModel)
        setConfiguration(rootDtoModelClass.simpleName!!, newConf)
        newConf.block()
    }

    fun <CHILD: LongEntity>childBinding(
        byProperty: KProperty1<ENTITY, SizedIterable<CHILD>>,
        referencedOnProperty: KMutableProperty1<CHILD, ENTITY>,
        type: OrdinanceType,
        body:  DTOClass<DATA,CHILD>.()-> Unit){


    }


}
