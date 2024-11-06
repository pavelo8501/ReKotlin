package po.db.data_service.services.models

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

abstract class ServiceDBEntity(id:  EntityID<Long>): LongEntity(id){

}




