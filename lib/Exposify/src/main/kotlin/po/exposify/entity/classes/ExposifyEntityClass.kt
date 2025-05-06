package po.exposify.entity.classes

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.entity.interfaces.EntityDTO


abstract class ExposifyEntityClass<out E : LongEntity>(
    val  sourceTable : IdTable<Long>,
    private val  entityTypeE: Class<E>? = null,
    private val  entityCtorE: ((EntityID<Long>) -> E)? = null
) : LongEntityClass<E>(sourceTable, entityTypeE, entityCtorE), EntityDTO{



}