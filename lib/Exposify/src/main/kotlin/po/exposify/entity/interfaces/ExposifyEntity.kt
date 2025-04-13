package po.exposify.entity.interfaces

import org.jetbrains.exposed.dao.LongEntity

interface ExposifyEntity {
   // val id: LongEntity get() = (this as LongEntity)
    val exposifyId: Long get() = (this as LongEntity).id.value
}