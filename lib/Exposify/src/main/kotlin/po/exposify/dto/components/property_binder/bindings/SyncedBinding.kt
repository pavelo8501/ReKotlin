package po.exposify.dto.components.property_binder.bindings

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.property_binder.enums.PropertyType
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.property_binder.interfaces.PropertyBindingOption
import po.exposify.entity.classes.ExposifyEntityBase
import kotlin.reflect.KMutableProperty1

class SyncedBinding<DATA : DataModel, ENT : ExposifyEntityBase, T>(
    override val dataProperty:KMutableProperty1<DATA, T>,
    val entityProperty:KMutableProperty1<ENT, T>
): PropertyBindingOption<DATA, ENT, T>
{
    override val propertyType: PropertyType = PropertyType.TWO_WAY

    private var onModelUpdatedCallback: ((PropertyBindingOption<DATA, ENT, T>) -> Unit)? = null
    override fun onModelUpdated(callback: (PropertyBindingOption<DATA, ENT, T>) -> Unit) {
        onModelUpdatedCallback = callback
    }

    private var onPropertyUpdatedCallback: ((String, PropertyType, UpdateMode) -> Unit)? = null
    override fun onPropertyUpdated(callback: (String, PropertyType, UpdateMode) -> Unit) {
        onPropertyUpdatedCallback = callback
    }

    var updated: Boolean = false
    override fun updated(
        name: String,
        type: PropertyType,
        updateMode: UpdateMode
    ) {
        updated = true
        onPropertyUpdatedCallback?.invoke(name, type, updateMode)
    }

    fun update(dtoModel: DATA, entityModel: ENT, mode: UpdateMode): Boolean {
        updated = false
        val dtoValue = dataProperty.get(dtoModel)
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
                    dataProperty.set(dtoModel, entityValue)
                    updated(dataProperty.name, propertyType, UpdateMode.ENTITY_TO_MODEL)
                    return true
                }
                return false
            }
            UpdateMode.ENTITY_TO_MODEL_FORCED -> {
                if(entityValue != null){
                    dataProperty.set(dtoModel, entityValue)
                    updated(dataProperty.name, propertyType, UpdateMode.ENTITY_TO_MODEL)
                    true
                }else{
                    false
                }
            }
            UpdateMode.MODEL_TO_ENTITY -> {
                if (!valuesDiffer) return false
                entityProperty.set(entityModel, dtoValue)
                true
            }
            UpdateMode.MODEL_TO_ENTITY_FORCED -> {
                if(entityValue != null) {
                    entityProperty.set(entityModel, dtoValue)
                    return true
                }
                return false
            }
        }
        if(updated){
            updated = false
            onModelUpdatedCallback?.invoke(this)
        }
    }
}