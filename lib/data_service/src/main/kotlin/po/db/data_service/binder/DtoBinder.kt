package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.DataModel
import kotlin.reflect.KMutableProperty1


class PropertyBinding<DATA_MODEL, ENTITY, TYPE>(
    val name : String,
    private val dtoProperty : KMutableProperty1<DATA_MODEL, TYPE>,
    private val entityProperty : KMutableProperty1<ENTITY, TYPE>) where  DATA_MODEL : DataModel, ENTITY : LongEntity
{

    fun update(dtoModel: DATA_MODEL, entityModel: ENTITY, force: Boolean = false): Boolean {
        val dtoValue = dtoProperty.get(dtoModel)
        val entityValue = entityProperty.get(entityModel)
        if (!force && dtoValue == entityValue) return false
        entityProperty.set(entityModel, dtoValue)
        return true
    }
}

class DTOPropertyBinder <DATA_MODEL, ENTITY>(
    vararg  props : PropertyBinding<DATA_MODEL, ENTITY, *> = emptyArray() )
        where DATA_MODEL :DataModel, ENTITY : LongEntity {

    private var propertyList = props.toList()

    fun setProperties(properties: List<PropertyBinding<DATA_MODEL, ENTITY, *>>){
        propertyList = properties
    }

    fun properties(vararg  props : PropertyBinding<DATA_MODEL, ENTITY, * >){
        setProperties(props.toList())
    }


    fun updateProperties(dataModel: DATA_MODEL, entityModel: ENTITY, force: Boolean = false) {
        propertyList.forEach { it.update(dataModel, entityModel, force) }
    }
}
