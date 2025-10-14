package po.exposify.dao.models

import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.misc.interfaces.Copyable
import po.misc.reflection.properties.PropertyMap
import kotlin.reflect.KProperty1

data class ColumnPropertyData(
    val columnData : ColumnMetadata,
    val property: KProperty1<IdTable<Long>, *>,
    val mapKey: String,
){
    val columnName: String get() = columnData.columnName
    val propertyName: String get()= property.name

    fun compareAndGet(lookUpName: String):ColumnPropertyData?{

        if(mapKey != columnData.columnName){
           throw OperationsException(this, "name != columnData.columnName", ExceptionCode.ABNORMAL_STATE)
        }

        if(columnData.columnName == lookUpName){
            return this
        }
        if(property.name == lookUpName){
            return this
        }
        return null
    }
}

class TableColumnMap(

) : PropertyMap<IdTable<Long>>(), Copyable<TableColumnMap> {

    private val columnMetadataBacking: MutableMap<String, ColumnMetadata> = mutableMapOf()
    val columnMetadata: Map<String, ColumnMetadata> = columnMetadataBacking

    fun addColumnMetadataAndProperty(name: String, metadata: ColumnMetadata, property: KProperty1<IdTable<Long>, *>){
        columnMetadataBacking.put(name, metadata)
        addProperty(name, property)
    }

    fun getData(): List<ColumnPropertyData>{
       return columnMetadata.mapNotNull {(key, value)->
            val prop = getPropertyByName(key)
            if(prop != null){
                ColumnPropertyData(value, prop, key)
            }else{
                null
            }
        }
    }

    fun getData(name: String): ColumnPropertyData?{
        val prop = getPropertyByName(name)
        val columnData = columnMetadata[name]
        if(prop != null && columnData != null){
          return  ColumnPropertyData(columnData, prop, name)
        }
        return null
    }

    override fun copy(): TableColumnMap {
        val newMap = TableColumnMap()
        for ((name, prop) in properties ) {
            val meta = columnMetadata[name]
            if (meta != null) {
                newMap.addColumnMetadataAndProperty(name, meta, prop)
            }
        }
        return newMap
    }
}