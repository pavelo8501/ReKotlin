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
import po.db.data_service.structure.ServiceContext
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


data class ModelEntityPairContainer<DATA_MODEL: DataModel, ENTITY : LongEntity>(
    val uniqueKey : String,
    val dataModel : AbstractDTOModel<DATA_MODEL, ENTITY>,
    val entityModel : LongEntityClass<ENTITY>
)

abstract class DTOClass<DATA_MODEL: DataModel, ENTITY : LongEntity>(){

    private var _serviceContext:ServiceContext<DATA_MODEL, ENTITY>? = null
    private val serviceContext: ServiceContext<DATA_MODEL, ENTITY>
        get() = _serviceContext ?: throw InitializationException(
            "DTO model should be configured inside  DTOClass configuration function",
            ExceptionCodes.INITIALIZATION_OUTSIDE_CONTEXT
        )

    fun initialize(
        dtoBlueprint : ConstructorBlueprint<DATA_MODEL>,
        context : ServiceContext<DATA_MODEL, ENTITY>
    ){
        this._serviceContext = context
    }

    abstract fun configuration()
    fun config(body: ModelDTOConfig<DATA_MODEL, ENTITY>.() ->  Unit) = serviceContext.config{
        body.invoke(this)
    }

    fun nowTime():LocalDateTime{
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }

    var initialClassCheckComplete = false

    fun create(dtoBlueprint : ConstructorBlueprint<DATA_MODEL> ):DATA_MODEL{

        if(dtoBlueprint.effectiveConstructor != null){
            val args = dtoBlueprint.effectiveConstructor!!.parameters.associateWith {
                dtoBlueprint.constructorParams[it.name] to ConstructorBuilder.getDefaultForType(it.type)
            }.toMap()
            return dtoBlueprint.effectiveConstructor!!.callBy(args)
        }else{
            dtoBlueprint.clazz.constructors.find { it.parameters.isEmpty() }?.let {
                return it.call()
            }
            throw  OperationsException("DTO entity creation failed, supplied class definition has no appropriate constructor", ExceptionCodes.NO_EMPTY_CONSTRUCTOR)
        }
    }

//    inline fun <reified DATA_MODEL : DataModel, ENTITY : LongEntity> create(dataModel: DATA_MODEL): AbstractDTOModel<DATA_MODEL, ENTITY> {
//        return object : AbstractDTOModel<DATA_MODEL, ENTITY>() {
//            override val id: Long = 0
//            override val entity: DATA_MODEL = dataModel
//        }
//    }
}




