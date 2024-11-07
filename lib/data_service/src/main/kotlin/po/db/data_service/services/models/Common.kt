package po.db.data_service.services.models

import org.jetbrains.exposed.dao.id.EntityID
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


interface IdContainingData {
    val id : Long
}

class ChildMapping<T : ServiceDataModel<E>, E : ServiceDBEntity>(
    val modelClass: ServiceDataModelClass<T, E>,
    val models: List<T>,
    val serviceName: String,
    var parentEntityId: EntityID<Long>?  = null
)


data class PropertyMapping(
    val property: KProperty1<ContainerModel<*>, Any?>,
    val dbPropName: String,
    val dbProperty: KMutableProperty1<Any, Any?>,
)