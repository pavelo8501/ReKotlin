package po.db.data_service.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.constructors.ClassBlueprint
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.Notificator
import po.db.data_service.structure.ServiceContext
import kotlin.reflect.KClass


data class ModelEntityPairContainer<DATA_MODEL, ENTITY>(
    val uniqueKey : String,
//    val dataModel : AbstractDTOModel<DataModel<DATA_MODEL>, ENTITY>,
//    val entityModel : LongEntityClass<ENTITY>
)



class DTOClassInnerContext<DATA_MODEL, ENTITY>(
    val configuration : ModelDTOConfig<DATA_MODEL, ENTITY>,
    subscribe :  (Notificator.()-> Unit)? = null): CanNotify  where   DATA_MODEL : DataModel, ENTITY : LongEntity{

    override val name = "DTOClassInnerContext"

    val notifications = Notificator(this)

    init{
        subscribe?.invoke(notifications)
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
}

class DTOClassOuterContext<DATA_MODEL, ENTITY>(
    private val configuration : ModelDTOConfig<DATA_MODEL, ENTITY>,
    notifications : (Notificator)-> Unit): CanNotify  where   DATA_MODEL : DataModel, ENTITY : LongEntity{

    override val name = "DTOClassOuterContext"

    val notificator : Notificator

    init {
        notificator = Notificator(this).also(notifications)
    }

   // val notifications = Notificator(this)

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
        notificator.triggerNotification()
    }

    fun setProperties(vararg props: PropertyBinding<DATA_MODEL, ENTITY, *>) = configuration.setProperties(props.toList())
    fun setDataModelConstructor(dataModelConstructor: () -> DATA_MODEL) = configuration.setDataModelConstructor(dataModelConstructor)

}

abstract class DTOClass<DATA_MODEL, ENTITY>() where DATA_MODEL : DataModel, ENTITY : LongEntity{

    private val dtoConfig  = ModelDTOConfig<DATA_MODEL, ENTITY>()

    private val innerContext = DTOClassInnerContext(dtoConfig)

    val outerContext = DTOClassOuterContext(dtoConfig){
       it.subscribe(this, it.notifications[2]){
            println("Callback Fired")
        }
    }

    private var  _dtoBlueprint : ClassBlueprint<DATA_MODEL>? = null
    private val dtoBlueprint : ClassBlueprint<DATA_MODEL>
        get(){
            return _dtoBlueprint?: throw InitializationException("dtoBlueprint", ExceptionCodes.NOT_INITIALIZED)
        }


    private var _serviceContext:ServiceContext<DATA_MODEL, ENTITY>? = null
    val serviceContext: ServiceContext<DATA_MODEL, ENTITY>
        get() = _serviceContext ?: throw InitializationException(
            "DTO model should be configured inside  DTOClass configuration function",
            ExceptionCodes.INITIALIZATION_OUTSIDE_CONTEXT
        )

    protected abstract fun configuration()

    //abstract fun configuration2(context : (DTOClassOuterContext<DATA_MODEL, ENTITY>)-> Unit )

    fun initializeRef(
        dtoBlueprint : ClassBlueprint<DATA_MODEL>,
        context : ServiceContext<DATA_MODEL, ENTITY>
    ){
        this._dtoBlueprint = dtoBlueprint
        this._serviceContext = context
    }

//    fun config()
    //    private  fun <T>conf(conf: DTOClassOuterContext<DATA_MODEL,ENTITY>, statement: DTOClassOuterContext<DATA_MODEL,ENTITY>.() -> T): T =   statement.invoke(outerContext)
    //    abstract fun <T>configuration(config : ()->Unit ):T
    //    private val con: (DTOClassOuterContext<DATA_MODEL, ENTITY>.() -> Unit) = {
//    }

    init {
        this.configuration()
    }

    fun initializeDTO(context: DTOClassInnerContext<DATA_MODEL, ENTITY>.() -> Unit){
        context(innerContext)
    }
//    inline fun <reified T:DataModel> config(noinline body: ModelDTOConfig<DATA_MODEL, ENTITY>.() ->  Unit) = serviceContext.config{
//        body.invoke(this)
//        dtoModelClass = T::class
//        val clazz =  T::class
//        val a = 10
//    }

    fun nowTime():LocalDateTime{
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }

    var initialClassCheckComplete = false

    fun <T : Any>create(daoEntity : ENTITY): T {
        val dataModel = try {
             val model = dtoConfig.dataModelConstructor?.invoke().let { model ->
                 model?: if (dtoBlueprint.effectiveConstructor != null) {
                         dtoBlueprint.effectiveConstructor!!.callBy(dtoBlueprint.constructorParams)
                     }else {
                         dtoBlueprint.clazz.constructors.find { it.parameters.isEmpty() }?.call()
                         throw OperationsException(
                             "DTO entity creation failed, supplied class definition has no appropriate constructor",
                             ExceptionCodes.NO_EMPTY_CONSTRUCTOR
                         )
                     }
                 }
             model
             }catch (ex: Exception) {
                throw OperationsException("DataModel  creation failed ${ex.message}", ExceptionCodes.REFLECTION_ERROR)
            }

            val dtoEntity = try {
                if(dtoBlueprint.effectiveConstructor != null) {
                   dtoBlueprint.effectiveConstructor!!.callBy(dtoBlueprint.constructorParams)
                }else {
                    dtoBlueprint.clazz.constructors.find { it.parameters.isEmpty() }?.call()
                    throw  OperationsException("DTO entity creation failed, supplied class definition has no appropriate constructor", ExceptionCodes.NO_EMPTY_CONSTRUCTOR)
                }
            }catch (ex:Exception){
                throw  OperationsException("DTO entity creation failed ${ex.message} ", ExceptionCodes.REFLECTION_ERROR)
            }

        return dtoEntity as T

//        return if(!initialClassCheckComplete){
//            if(model::class.isSubclassOf(AbstractDTOModel::class)){
//                initialClassCheckComplete = true
//                (model as AbstractDTOModel<DATA_MODEL, ENTITY>).also {
//                    it.entityDAO = daoEntity
//                }
//            }else{
//                throw OperationsException("Created model class does not derive from AbstractDTOModel", ExceptionCodes.REFLECTION_ERROR)
//            }
//        }else{
//            model as AbstractDTOModel<DATA_MODEL, ENTITY>
//        }
    }

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





