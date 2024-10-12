package po.db.data_service.services.models

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.exceptions.DataServiceException
import po.db.data_service.exceptions.ErrorCodes
import po.db.data_service.services.BasicDataService


open class ServiceDataModelClass<T: ServiceDataModel<E>, E: ServiceDBEntity>() {
    val nowDateTime = LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    var dataService: BasicDataService<T, E>? = null
    fun createModel() {

    }
    fun initDataService(dataService : BasicDataService<T,E>){
        this.dataService = dataService
    }
}

abstract class ServiceDataModel<E: ServiceDBEntity>(): IdContainingData{

    abstract  var sourceEntity: E?
    fun updateInt(value: Int?): Int?{
        return value
    }
    fun updateStringOrNull(value: String?): String?{
        return value
    }
    fun updateString(value: String): String{
        return value
    }
    fun updateId(value: Long): Long{
        return value
    }

   // abstract var parentService : BasicDataService<ServiceDataModel<E>,E>

    fun saveToEntity(entity: E):E {
       this.sourceEntity = entity
       triggerEntityUpdate()
       return entity
    }

    protected abstract fun updateEntity(): List<() -> Unit>
    val onEntityUpdate: List<() -> Unit> by lazy {
        updateEntity()
    }
    fun triggerEntityUpdate() {
        onEntityUpdate.forEach { updateFunction ->
            updateFunction.invoke()  // Invoke each lambda with the entity
        }
    }

    val hasEntity : Boolean
    get() = sourceEntity != null

    fun getEntity(update: Boolean):E {
        if(sourceEntity == null) throw DataServiceException("Source Entity uninitialized for model id: ${this.id}", ErrorCodes.NOT_INITIALIZED)
        if(update) triggerEntityUpdate()
        return this.sourceEntity!!
    }
    fun setEntity(entity: E){
        sourceEntity = entity
    }

}
