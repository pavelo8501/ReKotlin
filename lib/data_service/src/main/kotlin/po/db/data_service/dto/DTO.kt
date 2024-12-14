package po.db.data_service.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.binder.DTOPropertyBinder
import po.db.data_service.dto.interfaces.DAOWInstance
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.controls.NotificationEvent
import po.db.data_service.controls.subscribe
import po.db.data_service.dto.classes.DtoComponents
import po.db.data_service.models.CommonDTO
import po.db.data_service.scope.service.ServiceContext


data class ModelEntityPairContainer<DATA_MODEL, ENTITY>(
    val uniqueKey : String,
//    val dataModel : AbstractDTOModel<DataModel<DATA_MODEL>, ENTITY>,
//    val entityModel : LongEntityClass<ENTITY>
)


abstract class DTOClass<DATA_MODEL, ENTITY>(): DAOWInstance where DATA_MODEL : DataModel, ENTITY : LongEntity{

    private val dtoConfig  = ModelDTOConfig<DATA_MODEL, ENTITY>()

    val outerContext = DTOContext(dtoConfig)
    private val innerContext = DtoComponents(dtoConfig, outerContext)

    val daoEntityModel: LongEntityClass<ENTITY>
        get (){return outerContext.entityModel}

    fun create(daoENTITY: ENTITY) = innerContext.create(daoENTITY, this)

    fun update(dataModel: DATA_MODEL, entity: ENTITY) {
        onUpdateProperties?.invoke(dataModel, entity)
    }
    var onUpdateProperties :  ((dataModel: DATA_MODEL, entity: ENTITY)->Unit)? = null

   // abstract fun updateProperties(dataModel: DATA_MODEL, entity: ENTITY )

    protected abstract fun configuration()

    init {
        this.dtoConfig.subscribe<DTOClass<DATA_MODEL, ENTITY>,ModelDTOConfig<DATA_MODEL, ENTITY>, DTOPropertyBinder<DATA_MODEL, ENTITY>>(NotificationEvent.ON_INITIALIZED){ binder->
            binder?.update{
              //  this@DTOClass.onUpdateProperties = { dataModel, entity -> this.updateProperties(dataModel, entity) }
            }
        }
        this.configuration()
    }

    fun initializeDTO(serviceContext : ServiceContext<DATA_MODEL, ENTITY>, context: DtoComponents<DATA_MODEL, ENTITY>.() -> Unit){
        innerContext.serviceContext = serviceContext
        context(innerContext)
    }

    fun nowTime():LocalDateTime{
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }

    var initialClassCheckComplete = false
}

inline fun <reified DTO : CommonDTO<DATA_MODEL, ENTITY>,  reified DATA_MODEL, ENTITY>  DTOClass<DATA_MODEL, ENTITY>.initializeDTO(
    entityModel: LongEntityClass<ENTITY>,
    crossinline block: DTOContext<DATA_MODEL, ENTITY>.() -> Unit) where  DATA_MODEL : DataModel, ENTITY : LongEntity {
    outerContext.setDataModelClass(DATA_MODEL::class)
    outerContext.setDTOModelClass(DTO::class)
    outerContext.setEntityModel(entityModel)
    outerContext.setInitValues(DATA_MODEL::class, DTO::class, entityModel)
    block(outerContext)
}





