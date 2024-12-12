package po.db.data_service.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.constructors.ConstructorBlueprint
import po.db.data_service.constructors.ConstructorBuilder
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.structure.ConnectionContext
import po.db.data_service.structure.ServiceContext
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf


data class ModelEntityPairContainer<DATA_MODEL: DataModel, ENTITY : LongEntity>(
    val uniqueKey : String,
    val dataModel : AbstractDTOModel<DATA_MODEL, ENTITY>,
    val entityModel : LongEntityClass<ENTITY>
)

abstract class DTOClass<DATA_MODEL: DataModel, ENTITY : LongEntity>(val entityModel: LongEntityClass<ENTITY>){

    val configuration  = ModelDTOConfig<DATA_MODEL, ENTITY>()

    var dtoModelClassName : String = "undefined"
    private var _dtoModelClass : KClass<*>? = null
    var dtoModelClass : KClass<*>
        get(){
            return _dtoModelClass?:throw InitializationException("fail", ExceptionCodes.NOT_INITIALIZED)
        }
        set(value){
            this._dtoModelClass = value
            dtoModelClassName = this._dtoModelClass?.qualifiedName.toString()
        }

    private var  _dtoBlueprint : ConstructorBlueprint<DATA_MODEL>? = null
    private val dtoBlueprint : ConstructorBlueprint<DATA_MODEL>
        get(){
            return _dtoBlueprint?: throw InitializationException("dtoBlueprint", ExceptionCodes.NOT_INITIALIZED)
        }

    private var _serviceContext:ServiceContext<DATA_MODEL, ENTITY>? = null
    val serviceContext: ServiceContext<DATA_MODEL, ENTITY>
        get() = _serviceContext ?: throw InitializationException(
            "DTO model should be configured inside  DTOClass configuration function",
            ExceptionCodes.INITIALIZATION_OUTSIDE_CONTEXT
        )

    fun initialize(
        dtoBlueprint : ConstructorBlueprint<DATA_MODEL>,
        context : ServiceContext<DATA_MODEL, ENTITY>
    ){
        this._dtoBlueprint = dtoBlueprint
        this._serviceContext = context
    }

    abstract fun configuration()

    inline fun <reified T : DATA_MODEL> config(
        noinline body: ModelDTOConfig<DATA_MODEL, ENTITY>.() ->  Unit
    ){
        body(configuration)
        dtoModelClass = T::class
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

    fun create(daoEntity : ENTITY): DATA_MODEL {
            val model = try {
                if(dtoBlueprint.effectiveConstructor != null) {
                   dtoBlueprint.effectiveConstructor!!.callBy(dtoBlueprint.constructorParams)
                }else {
                    dtoBlueprint.clazz.constructors.find { it.parameters.isEmpty() }?.call()
                    throw  OperationsException("DTO entity creation failed, supplied class definition has no appropriate constructor", ExceptionCodes.NO_EMPTY_CONSTRUCTOR)
                }
            }catch (ex:Exception){
                throw  OperationsException("DTO entity creation failed ${ex.message} ", ExceptionCodes.REFLECTION_ERROR)
            }

        return model

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
}




