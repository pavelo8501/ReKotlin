package po.exposify.dao.interfaces

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.EntityID

interface EntityDTO {
    val exposifyId: EntityID<Long> get() = (this as LongEntity).id
}