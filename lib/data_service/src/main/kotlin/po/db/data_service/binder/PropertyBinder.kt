package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.interfaces.DataModel
import kotlin.reflect.KMutableProperty1


enum class UpdateMode{
    ENTITY_TO_MODEL,
    ENTITY_TO_MODEL_FORCED,
    MODEL_TO_ENTNTY,
    MODEL_TO_ENTNTY_FORCENTD,
}

class PropertyBinding<DATA : DataModel, ENT : LongEntity, T>(
    private val dtoProperty: KMutableProperty1<DATA, T>,
    private val entityProperty: KMutableProperty1<ENT, T>

){
     fun update(dtoModel: DATA, entityModel: ENT, mode: UpdateMode): Boolean {

        val dtoValue = dtoProperty.get(dtoModel)
        val entityValue =  entityProperty.get(entityModel)
        val valuesDiffer = dtoValue != entityValue

        return when (mode) {
            UpdateMode.ENTITY_TO_MODEL -> {
                if (!valuesDiffer) return false
                if(entityValue != null){
                    dtoProperty.set(dtoModel, entityValue)
                    return true
                }
                return false
            }
            UpdateMode.MODEL_TO_ENTNTY -> {
                if (!valuesDiffer) return false
                entityProperty.set(entityModel, dtoValue)
                true
            }
            UpdateMode.MODEL_TO_ENTNTY_FORCENTD -> {
                if(entityValue != null) {
                    dtoProperty.set(dtoModel, entityValue)
                    return true
                }
                return false
            }
            UpdateMode.ENTITY_TO_MODEL_FORCED -> {
                entityProperty.set(entityModel, dtoValue)
                true
            }
        }
    }
}

class PropertyBinder<DATA : DataModel, ENT : LongEntity>  {

    var onInitialized: ((PropertyBinder<DATA, ENT>) -> Unit)? = null
    private var propertyList = emptyList<PropertyBinding<DATA, ENT, *>> ()

    fun setProperties(properties: List<PropertyBinding<DATA, ENT, *>> ) {
        propertyList = properties
        onInitialized?.invoke(this)
    }

    fun update(dataModel: DATA, daoModel: ENT, updateMode: UpdateMode) {
        propertyList.forEach { it.update(dataModel, daoModel, updateMode) }
    }
}