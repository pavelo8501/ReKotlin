package po.exposify.entity.interfaces

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.EntityID

interface ExposifyEntity {
    val exposifyId: EntityID<Long> get() = (this as LongEntity).id
}