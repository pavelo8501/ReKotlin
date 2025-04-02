package po.exposify.entity.classes

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.EntityID
import po.exposify.entity.interfaces.ExposifyEntity

abstract class ExposifyEntityBase(id: EntityID<Long>) : LongEntity(id) , ExposifyEntity{

}