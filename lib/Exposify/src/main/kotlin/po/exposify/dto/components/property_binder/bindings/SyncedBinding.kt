package po.exposify.dto.components.property_binder.bindings

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.property_binder.enums.PropertyType
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.property_binder.interfaces.PropertyBindingOption
import po.exposify.entity.classes.ExposifyEntityBase
import kotlin.reflect.KMutableProperty1

class SyncedBinding<DATA : DataModel, ENT : ExposifyEntityBase, T>(
    override val dataProperty:KMutableProperty1<DATA, T>,
    override val referencedProperty :KMutableProperty1<ENT, T>
): PropertyBindingOption<DATA, ENT, T>
{
    override val propertyType: PropertyType = PropertyType.TWO_WAY

    override var onDataUpdatedCallback: ((PropertyBindingOption<DATA, ENT, T>) -> Unit)? = null

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
            referencedProperty.get(entityModel)
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
                referencedProperty.set(entityModel, dtoValue)
                true
            }
            UpdateMode.MODEL_TO_ENTITY_FORCED -> {
                if(entityValue != null) {
                    referencedProperty.set(entityModel, dtoValue)
                    return true
                }
                return false
            }
        }
        if(updated){
            updated = false
            onDataUpdatedCallback?.invoke(this)
        }
    }
}