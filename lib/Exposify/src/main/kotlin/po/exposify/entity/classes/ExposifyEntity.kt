package po.exposify.entity.classes

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import po.exposify.entity.interfaces.EntityDTO


abstract class ExposifyEntity<T: LongIdTable<>>(id: EntityID<Long>) : LongEntity(id) , EntityDTO{





}