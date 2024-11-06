package po.db.data_service.services.models

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.exceptions.DataServiceException
import po.db.data_service.exceptions.ErrorCodes
import po.db.data_service.services.BasicDataService

open class ServiceDataModelClass<T : ServiceDataModel<E>, E : ServiceDBEntity>(val entityClass: LongEntityClass<E>) {

    val nowDateTime = LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    var dataService: BasicDataService<T,E>? = null
    val companionObject: Any = this
    fun createModel() {

    }
    fun initDataService(dataService : BasicDataService<T,E>){
        this.dataService = dataService
    }

    fun saveChildModels(childModels : List<ServiceDataModel<E>>) {
        if(dataService == null) throw DataServiceException("Data Service not initialized for model class: ${this::class.simpleName}", ErrorCodes.NOT_INITIALIZED)
        childModels.forEach { model ->
            dataService!!.saveModel(model as T)
        }
    }

    fun saveChildMapping(childMapping :  ChildMapping<out ServiceDataModel<*>, out ServiceDBEntity>, parentEntityId : EntityID<Long>? = null) {
        if(dataService == null) throw DataServiceException("Data Service not initialized for model class: ${this::class.simpleName}", ErrorCodes.NOT_INITIALIZED)
        childMapping.models.forEach { model ->

            dataService!!.saveModelForParent(model as T, parentEntityId!!)
        }
    }
}

abstract class ServiceDataModel<E : ServiceDBEntity>: IdContainingData{

    fun updateInt(value: Int): Int?{
        return value
    }
    fun updateStringOrNull(value: String?): String?{
        return value
    }
    fun updateString(value: String): String{
        return value
    }
    fun updateBoolean(value: Boolean): Boolean{
        return value
    }
    fun updateId(value: Long): Long{
        return value
    }
    fun updateParentEntityId(value: EntityID<Long>): EntityID<Long>{
        return value
    }

    abstract var sourceEntity: E?

    abstract var parentEntityId : EntityID<Long>?

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


    open val childMapping: List<ChildMapping<out ServiceDataModel<*>, out ServiceDBEntity>> = listOf()

    val hasEntity : Boolean
    get() = sourceEntity != null

    fun getEntity(update: Boolean):E {
        if(sourceEntity == null) throw DataServiceException("Source Entity uninitialized for model id: ${this.id}", ErrorCodes.NOT_INITIALIZED)
        if(update) triggerEntityUpdate()
        return this.sourceEntity!!
    }
    open fun setEntity(entity: E){
        sourceEntity = entity
    }

    @Transient
    var initialized : Boolean = false

}


