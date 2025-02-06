package po.exposify.binder

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.classes.interfaces.DataModel
import kotlin.reflect.KMutableProperty1


enum class UpdateMode{
    ENTITY_TO_MODEL,
    ENTITY_TO_MODEL_FORCED,
    MODEL_TO_ENTNTY,
    MODEL_TO_ENTNTY_FORCENTD,
}

class PropertyBinding<DATA : DataModel, ENT : LongEntity, T>(
    internal val dtoProperty: KMutableProperty1<DATA, T>,
    internal val entityProperty: KMutableProperty1<ENT, T>
){
    fun update(dtoModel: DATA, entityModel: ENT, mode: UpdateMode): Boolean {

        val dtoValue = dtoProperty.get(dtoModel)

       val entityValue =  try {
            entityProperty.get(entityModel)
        }catch (ex: Exception){
            null
        }
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
    var propertyList = listOf<PropertyBinding<DATA, ENT, *>> ()
        private set

    fun setProperties(properties: List<PropertyBinding<DATA, ENT, *>> ) {
        propertyList = properties
        onInitialized?.invoke(this)
    }

    fun update(dataModel: DATA, daoModel: ENT, updateMode: UpdateMode) {
        try {
            propertyList.forEach { it.update(dataModel, daoModel, updateMode) }
        }catch (ex: Exception){
            println("Property Binder: ${ex.message}")
            throw ex
        }
    }
}