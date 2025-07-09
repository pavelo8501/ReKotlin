package po.exposify.dao.classes

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.isAutoInc
import po.exposify.dao.interfaces.EntityDTO
import po.exposify.extensions.castOrInit
import po.misc.data.ColumnMetadata
import po.misc.reflection.mappers.models.PropertyRecord
import po.misc.types.castOrManaged
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure


abstract class ExposifyEntityClass<out E : LongEntity>(
    val  sourceTable : IdTable<Long>,
    private val  entityTypeE: Class<E>? = null,
    private val  entityCtorE: ((EntityID<Long>) -> E)? = null
) : LongEntityClass<E>(sourceTable, entityTypeE, entityCtorE), EntityDTO {

    val <E : LongEntity> ExposifyEntityClass<E>.entityTable: IdTable<Long>
        get() = this.sourceTable

    @PublishedApi
    internal val columnNamePropertyMap = table::class.memberProperties
        .filter { it.returnType.jvmErasure.isSubclassOf(Column::class) }
        .associate { prop ->
            val column = prop.getter.call(table) as Column<*>
            column.name to prop
        }

    inline fun <reified E : LongEntity> analyzeExposedTableMetadata(): List<ColumnMetadata> {

        return sourceTable.columns.map { column ->
            val columnType = column.columnType
            val isForeignKey = column.referee != null

            val metaData = ColumnMetadata(
                columnName = column.name,
                type = columnType.sqlType(),
                isNullable = columnType.nullable,
                isPrimaryKey = table.primaryKey?.columns?.contains(column) == true,
                hasDefault = column.defaultValueFun != null,
                isAutoIncrement = columnType.isAutoInc,
                isForeignKey = isForeignKey,
                referencedTable = column.referee?.table?.tableName)

            metaData.propertyRecord = columnNamePropertyMap[column.name]?.let {
                val casted = it.castOrManaged<KProperty<E>>()
                PropertyRecord.create(casted)
            }
            metaData
        }
    }
}