package po.exposify.dto.components.property_binder.bindings

import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.components.property_binder.enums.PropertyType
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.property_binder.interfaces.PropertyBindingOption
import po.exposify.entity.classes.ExposifyEntity
import kotlin.reflect.KMutableProperty1

class SyncedBinding<DATA : DataModel, ENT : ExposifyEntity, T>(
    override val dataProperty:KMutableProperty1<DATA, T>,
    override val referencedProperty :KMutableProperty1<ENT, T>
): PropertyBindingOption<DATA, ENT, T>
{
    override val propertyType: PropertyType = PropertyType.TWO_WAY

    fun update(dtoModel: DATA, entityModel: ENT, mode: UpdateMode,  callback: ((String, PropertyType, UpdateMode) -> Unit)?): Boolean {
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
                    callback?.invoke(dataProperty.name, propertyType, UpdateMode.ENTITY_TO_MODEL)

                    return true
                }
                return false
            }
            UpdateMode.ENTITY_TO_MODEL_FORCED -> {
                if(entityValue != null){
                    dataProperty.set(dtoModel, entityValue)
                    callback?.invoke(dataProperty.name, propertyType, UpdateMode.ENTITY_TO_MODEL)
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
    }
}