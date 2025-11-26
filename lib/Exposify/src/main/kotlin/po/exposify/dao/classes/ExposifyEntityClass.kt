package po.exposify.dao.classes

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.isAutoInc
import po.exposify.dao.interfaces.EntityDTO
import po.exposify.dao.models.ColumnMetadata
import po.exposify.dao.models.TableColumnMap
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure


abstract class ExposifyEntityClass<E : LongEntity>(
    val  sourceTable : IdTable<Long>,
    private val  entityType: Class<E>? = null,
    private val  entityCtor: ((EntityID<Long>) -> E)? = null
) : LongEntityClass<E>(sourceTable, entityType, entityCtor), EntityDTO {

    val <E : LongEntity> ExposifyEntityClass<E>.entityTable: IdTable<Long>
        get() = this.sourceTable


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
            metaData
        }
    }


    fun createColumnMetadata(columnName: String): ColumnMetadata? {
        val found = sourceTable.columns.firstOrNull { it.name == columnName }
        if (found != null) {
            val columnType = found.columnType
            val isForeignKey = found.referee != null
            return ColumnMetadata(
                columnName = found.name,
                type = columnType.sqlType(),
                isNullable = columnType.nullable,
                isPrimaryKey = table.primaryKey?.columns?.contains(found) == true,
                hasDefault = found.defaultValueFun != null,
                isAutoIncrement = columnType.isAutoInc,
                isForeignKey = isForeignKey,
                referencedTable = found.referee?.table?.tableName,
            )
        }
        return null
    }

    fun createTablePropertyMap(): TableColumnMap{
       val tableColumnMap = TableColumnMap()
        table::class.memberProperties
            .filter { it.returnType.jvmErasure.isSubclassOf(Column::class) }.forEach { prop ->
                val column = prop.getter.call(table) as Column<*>
                val metadata = createColumnMetadata(column.name)
                if(metadata != null){
                    @Suppress("Unchecked_CAST")
                    tableColumnMap.addColumnMetadataAndProperty(column.name, metadata, prop as KProperty1<IdTable<Long>, *>)
                }
            }
        return tableColumnMap
    }

}