package po.db.data_service.dto

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.dto.components.DTOComponents
import po.db.data_service.models.CommonDTO
import po.db.data_service.scope.service.controls.service_registry.DTOData
import kotlin.reflect.KClass


data class ModelEntityPairContainer<DATA_MODEL, ENTITY>(
    val uniqueKey : String,
//    val dataModel : AbstractDTOModel<DataModel<DATA_MODEL>, ENTITY>,
//    val entityModel : LongEntityClass<ENTITY>
)


abstract class DTOClass<DATA_MODEL, ENTITY>() where DATA_MODEL : DataModel, ENTITY : LongEntity{

   val dtoContext = DTOContext<DATA_MODEL, ENTITY>()
   val dtoComponents = DTOComponents<DATA_MODEL, ENTITY>()

    val daoEntityModel: LongEntityClass<ENTITY>
        get (){return dtoContext.entityModel}

    fun create(daoENTITY: ENTITY) = dtoComponents.create(daoENTITY, this)
    fun update(dataModel: DATA_MODEL, entity: ENTITY) {
        onUpdateProperties?.invoke(dataModel, entity)
    }
    var onUpdateProperties :  ((dataModel: DATA_MODEL, entity: ENTITY)->Unit)? = null

   // abstract fun updateProperties(dataModel: DATA_MODEL, entity: ENTITY )

    protected abstract fun configuration()

    var onInitialized : ((DTOData<DATA_MODEL, ENTITY>)-> Unit)? = null

    fun initialization(callback: (DTOData<DATA_MODEL, ENTITY>)-> Unit): DTOClass<DATA_MODEL, ENTITY>{
        onInitialized = callback
        configuration()
       // val dtoData = DTOData(dtoContext.dtoModelClass, dtoContext.entityModel, dtoContext.dataModelClass)
       // onInitialized.invoke(dtoData)
        return this
    }

    fun nowTime():LocalDateTime{
        return LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    }
}

inline fun <reified DTO : CommonDTO<DATA_MODEL, ENTITY>, reified DATA_MODEL, reified ENTITY>  DTOClass<DATA_MODEL, ENTITY>.initializeDTO(
    entityModel: LongEntityClass<ENTITY>,
    block: DTOContext<DATA_MODEL, ENTITY> .() -> Unit) where DATA_MODEL : DataModel, ENTITY : LongEntity{

    val dtoData = dtoContext.setInitValues(DATA_MODEL::class, CommonDTO::class as KClass<CommonDTO<DATA_MODEL, ENTITY>>, entityModel)
    block(dtoContext)
    onInitialized?.invoke(dtoData)
}





