package po.db.data_service.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.structure.ServiceContext


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
    fun setContext(context : ServiceContext<DATA_MODEL, ENTITY>) {
        this._serviceContext = context
    }

    abstract fun configuration()
    fun config(body: ModelDTOConfig<DATA_MODEL,ENTITY>.() ->  Unit) = serviceContext.config{
        body.invoke(this)
    }

    fun nowTime():LocalDateTime{
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }

    fun initializeEntity(entityDTO : AbstractDTOModel<DATA_MODEL, ENTITY>){
        if(entityDTO.id == 0L ){

        }
    }

//    inline fun <reified DATA_MODEL : DataModel, ENTITY : LongEntity> create(dataModel: DATA_MODEL): AbstractDTOModel<DATA_MODEL, ENTITY> {
//        return object : AbstractDTOModel<DATA_MODEL, ENTITY>() {
//            override val id: Long = 0
//            override val entity: DATA_MODEL = dataModel
//        }
//    }
}




