package po.exposify.dao.classes

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dao.interfaces.EntityDTO


abstract class ExposifyEntityClass<out E : LongEntity>(
    val  sourceTable : IdTable<Long>,
    private val  entityTypeE: Class<E>? = null,
    private val  entityCtorE: ((EntityID<Long>) -> E)? = null
) : LongEntityClass<E>(sourceTable, entityTypeE, entityCtorE), EntityDTO{


    init {
        println("ExposifyEntityClass initialized")
    }


    val <E : LongEntity> ExposifyEntityClass<E>.entityTable: IdTable<Long>
        get() = this.sourceTable

}