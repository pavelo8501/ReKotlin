package po.db.data_service.services.models

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable


interface IdContainingData {
    val id : Long
}

class ChildMapping<T : ServiceDataModel<E>, E : ServiceDBEntity>(
    val modelClass: ServiceDataModelClass<T, E>,
    val models: List<T>,
    val serviceName: String,
    var parentEntityId: EntityID<Long>?  = null
)