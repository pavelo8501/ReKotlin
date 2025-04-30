package po.exposify.dto.components.property_binder.bindings

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.property_binder.enums.PropertyType
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.property_binder.interfaces.PropertyBindingOption
import po.exposify.entity.classes.ExposifyEntity
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1



class ReadOnlyBinding<DATA : DataModel, ENT : ExposifyEntity, T>(
    override val dataProperty: KMutableProperty1<DATA, T>,
    override val referencedProperty: KProperty1<ENT, T>,
): PropertyBindingOption<DATA, ENT, T>
{
    override val propertyType: PropertyType = PropertyType.READONLY



    var updated: Boolean = false
    fun updated(
        name: String,
        type: PropertyType,
        updateMode: UpdateMode,
        onDataUpdatedCallback :  ((String, PropertyType, UpdateMode) -> Unit)??
    ) {
        updated = true
        onDataUpdatedCallback?.invoke(name, type, updateMode)
    }

   fun update(dtoModel: DATA, entityModel: ENT, mode: UpdateMode, onUpdate :  ((String, PropertyType, UpdateMode) -> Unit)?): Boolean {
        updated = false
        val dtoValue = dataProperty.get(dtoModel)
        val entityValue =  try {
            referencedProperty.get(entityModel)
        }catch (ex: Exception){
            null
        }
        val valuesDiffer = dtoValue != entityValue

        return when (mode) {
            UpdateMode.MODEL_TO_ENTITY -> {
                if (!valuesDiffer) return false
                if(entityValue != null){
                    dataProperty.set(dtoModel, entityValue)
                    updated(dataProperty.name, propertyType, UpdateMode.MODEL_TO_ENTITY, null)
                    return true
                }
                return false
            }

            UpdateMode.MODEL_TO_ENTITY_FORCED -> {
                if(entityValue != null) {
                    dataProperty.set(dtoModel, entityValue)
                    updated(dataProperty.name, propertyType, UpdateMode.MODEL_TO_ENTITY_FORCED, null)
                    return true
                }
                return false
            }
            else -> { false  }
        }

    }
}