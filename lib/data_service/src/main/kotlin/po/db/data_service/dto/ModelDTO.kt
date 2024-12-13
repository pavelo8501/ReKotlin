package po.db.data_service.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.binder.DTOPropertyBinder
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.constructors.ClassBlueprint
import po.db.data_service.constructors.ConstructorBuilder
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.NotificationEvent
import po.db.data_service.models.Notificator
import po.db.data_service.models.subscribe
import po.db.data_service.structure.ServiceContext
import kotlin.reflect.KClass


data class ModelEntityPairContainer<DATA_MODEL, ENTITY>(
    val uniqueKey : String,
//    val dataModel : AbstractDTOModel<DataModel<DATA_MODEL>, ENTITY>,
//    val entityModel : LongEntityClass<ENTITY>
)

enum class ContextState{
    UNINITIALIZED,
    BEFORE_INITIALIZATION,
    INITIALIZED,
    INITIALIZATION_FAILURE
}

class DTOClassInnerContext<DATA_MODEL, ENTITY>(
    private val configuration : ModelDTOConfig<DATA_MODEL, ENTITY>,
    val outerContext : DTOClassOuterContext<DATA_MODEL, ENTITY>,
    notifications: ((Notificator)-> Unit)? = null ): CanNotify  where   DATA_MODEL : DataModel, ENTITY : LongEntity{

    companion object : ConstructorBuilder()

    override val name = "DTOClassInnerContext"
    override var notificator = Notificator(this)

    init {
        if(notifications != null){
            notificator  = Notificator(this).also(notifications)
        }
    }

    private var  _dtoModelBlueprint : ClassBlueprint<CommonDTO<DATA_MODEL, ENTITY>>? = null
    private val dtoModelBlueprint : ClassBlueprint<CommonDTO<DATA_MODEL, ENTITY>>
        get(){
            return _dtoModelBlueprint?: throw InitializationException("dtoModelBlueprint requested but not initialized", ExceptionCodes.LAZY_NOT_INITIALIZED)
        }

    private var  _dataModelBlueprint : ClassBlueprint<DATA_MODEL>? = null
    private val dataModelBlueprint : ClassBlueprint<DATA_MODEL>
        get(){
            return _dataModelBlueprint?: throw InitializationException("dataModelBlueprint requested but not initialized", ExceptionCodes.LAZY_NOT_INITIALIZED)
        }
    fun setBlueprints(dtoBluePrint : ClassBlueprint<CommonDTO<DATA_MODEL, ENTITY>>, dataModelBlueprint : ClassBlueprint<DATA_MODEL>?){
        _dtoModelBlueprint = dtoBluePrint
        _dataModelBlueprint = dataModelBlueprint
    }

    var serviceContext: ServiceContext<DATA_MODEL, ENTITY>? = null
        get() {return field }
        set(value){
            field = value
        }

    fun create(daoEntity : ENTITY, dtoModel: DTOClass<DATA_MODEL, ENTITY> ): CommonDTO<DATA_MODEL, ENTITY> {
        val dataModel = try {
            val model = configuration.dataModelConstructor?.invoke().let { model ->
                model?: dataModelBlueprint.getEffectiveConstructor().callBy(dataModelBlueprint.constructorParams)
            }
            model
        }catch (ex: Exception) {
            throw OperationsException("DataModel  creation failed ${ex.message}", ExceptionCodes.REFLECTION_ERROR)
        }

        val dtoEntity = try {
            val params = dtoModelBlueprint.constructorParams
            val args =  getArgsForConstructor(dtoModelBlueprint){
                 when(it){
                     "dataModel"->{
                         dataModel
                     }
                     else->{null}
                 }
             }
             dtoModelBlueprint.getEffectiveConstructor().callBy(args)
        }catch (ex:Exception){
            throw  OperationsException("DTO entity creation failed ${ex.message} ", ExceptionCodes.REFLECTION_ERROR)
        }
        dtoEntity.setEntityDAO(daoEntity,dtoModel)
        return dtoEntity
    }
}

class DTOClassOuterContext<DATA_MODEL, ENTITY>(
    private val configuration : ModelDTOConfig<DATA_MODEL, ENTITY>) : CanNotify  where   DATA_MODEL : DataModel, ENTITY : LongEntity{

    var state:ContextState = ContextState.UNINITIALIZED
        set(value){
            if(field!=value){
                field = value
                when(field){
                    ContextState.INITIALIZED->{
                        notificator.trigger<Unit>(NotificationEvent.ON_INITIALIZED)
                    }
                    else->{}
                }
            }
        }

    override val name = "DTOClassOuterContext"
    override var notificator  = Notificator(this)

    init {

    }
    private var dtoModelClassName : String = "undefined"

    private var _entityModel: LongEntityClass<ENTITY>? = null
    val entityModel: LongEntityClass<ENTITY>
        get(){return _entityModel?: throw InitializationException("EntityModel requested but not initialized", ExceptionCodes.LAZY_NOT_INITIALIZED) }
    fun setEntityModel(entityModel : LongEntityClass<ENTITY>){
        _entityModel = entityModel
    }

    private var _dataModel: DATA_MODEL? = null
    val dataModel: DATA_MODEL
        get(){return _dataModel?: throw InitializationException("DataModel requested but not initialized", ExceptionCodes.LAZY_NOT_INITIALIZED) }
    fun setDataModel(dataModel : DATA_MODEL){
        _dataModel = dataModel
    }

    private var _dataModelClass : KClass<DATA_MODEL>? = null
    val dataModelClass:  KClass<DATA_MODEL>
        get(){return _dataModelClass?: throw InitializationException("DataModelClass requested but not initialized", ExceptionCodes.LAZY_NOT_INITIALIZED) }
    fun setDataModelClass(clazz : KClass<DATA_MODEL>){
        _dataModelClass = clazz
        dtoModelClassName = dataModelClass.qualifiedName?: "undefined"
    }

    private var _dtoModelClass : KClass<CommonDTO<DATA_MODEL, ENTITY>>? = null
    val dtoModelClass: KClass<CommonDTO<DATA_MODEL, ENTITY>>
        get(){return _dtoModelClass?: throw InitializationException("DTOModelClass requested but not initialized", ExceptionCodes.LAZY_NOT_INITIALIZED) }
    fun <DTO : CommonDTO<DATA_MODEL, ENTITY> >setDTOModelClass(clazz : KClass<DTO>){
        _dtoModelClass = clazz as KClass<CommonDTO<DATA_MODEL, ENTITY>>
    }

    fun <DTO : CommonDTO<DATA_MODEL, ENTITY>>setInitValues(dataModel : KClass<DATA_MODEL>, dtoModel : KClass<DTO>,  daoEntityModel : LongEntityClass<ENTITY> ){
        setDataModelClass(dataModel)
        setDTOModelClass(dtoModel)
        setEntityModel(daoEntityModel)
        state = ContextState.INITIALIZED
    }

    fun setProperties(vararg props: PropertyBinding<DATA_MODEL, ENTITY, *>) = configuration.setProperties(props.toList())
    fun setDataModelConstructor(dataModelConstructor: () -> DATA_MODEL) = configuration.setDataModelConstructor(dataModelConstructor)

}

abstract class DTOClass<DATA_MODEL, ENTITY>(): DAOWInstance where DATA_MODEL : DataModel, ENTITY : LongEntity{

    private val dtoConfig  = ModelDTOConfig<DATA_MODEL, ENTITY>()


    val outerContext = DTOClassOuterContext(dtoConfig)
    private val innerContext = DTOClassInnerContext(dtoConfig, outerContext)

    val daoEntityModel: LongEntityClass<ENTITY>
        get (){return outerContext.entityModel}

    fun create(daoENTITY: ENTITY) = innerContext.create(daoENTITY, this)

    fun update(dataModel: DATA_MODEL, entity: ENTITY) {
        onUpdateProperties?.invoke(dataModel, entity)
    }
    var  onUpdateProperties :  ((dataModel: DATA_MODEL, entity: ENTITY)->Unit)? = null

   // abstract fun updateProperties(dataModel: DATA_MODEL, entity: ENTITY )

    protected abstract fun configuration()

    init {
        this.dtoConfig.subscribe<DTOClass<DATA_MODEL, ENTITY>,ModelDTOConfig<DATA_MODEL, ENTITY>, DTOPropertyBinder<DATA_MODEL, ENTITY>>(NotificationEvent.ON_INITIALIZED){ binder->
            binder?.update{
                this@DTOClass.onUpdateProperties = { dataModel, entity -> this.updateProperties(dataModel, entity) }
            }
        }

        this.configuration()
    }

    fun initializeDTO(serviceContext : ServiceContext<DATA_MODEL, ENTITY>,  context: DTOClassInnerContext<DATA_MODEL, ENTITY>.() -> Unit){
        innerContext.serviceContext = serviceContext
        context(innerContext)
    }

    fun nowTime():LocalDateTime{
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }

    var initialClassCheckComplete = false

    inline fun <reified DTO : CommonDTO<DATA_MODEL, ENTITY>,  reified DATA_MODEL, ENTITY>  DTOClass<DATA_MODEL, ENTITY>.initializeDTO(
        entityModel: LongEntityClass<ENTITY>,
        crossinline block: DTOClassOuterContext<DATA_MODEL, ENTITY>.() -> Unit
    ) where  DATA_MODEL : DataModel , ENTITY : LongEntity {
        outerContext.setDataModelClass(DATA_MODEL::class)
        outerContext.setDTOModelClass(DTO::class)
        outerContext.setEntityModel(entityModel)
        outerContext.setInitValues(DATA_MODEL::class, DTO::class, entityModel)
        block(outerContext)
    }

}





