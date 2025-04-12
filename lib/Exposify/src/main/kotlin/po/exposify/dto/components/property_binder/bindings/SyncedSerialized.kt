package po.exposify.dto.components.property_binder.bindings

import kotlinx.serialization.KSerializer
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.property_binder.enums.PropertyType
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.property_binder.interfaces.PropertyBindingOption
import po.exposify.entity.classes.ExposifyEntityBase
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

class SyncedSerialized<DATA : DataModel, ENT : ExposifyEntityBase, C, TYPE: Any >(
    override val dataProperty:KMutableProperty1<DATA, C>,
    val entityProperty:KMutableProperty1<ENT, C>,
    internal val serializer:  KSerializer<TYPE>,
) : PropertyBindingOption<DATA, ENT, C>
{
    override val propertyType: PropertyType = PropertyType.SERIALIZED
    private var onModelUpdatedCallback: ((PropertyBindingOption<DATA, ENT, C>) -> Unit)? = null
    override fun onModelUpdated(callback: (PropertyBindingOption<DATA, ENT, C>) -> Unit) {
        onModelUpdatedCallback = callback
    }

    private var onPropertyUpdatedCallback: ((String, PropertyType, UpdateMode) -> Unit)? = null
    override fun onPropertyUpdated(callback: (String, PropertyType, UpdateMode) -> Unit) {
        onPropertyUpdatedCallback = callback
    }

    var updated: Boolean = false
    override fun updated(name: String, type: PropertyType, updateMode: UpdateMode) {
        updated = true
        onPropertyUpdatedCallback?.invoke(name, type, updateMode)
    }

    fun getSerializer(): Pair<String, KSerializer<TYPE>>{
        return Pair(dataProperty.name, serializer)
    }

    fun update(dtoModel: DATA, entityModel: ENT, mode: UpdateMode): Boolean {
        updated = false
        val dtoValue = dataProperty.get(dtoModel)

        val entityValue = try {
            entityProperty.get(entityModel)
        } catch (ex: Exception) {
            null
        }
        val valuesDiffer = dtoValue != entityValue

        val result = when (mode) {
            UpdateMode.ENTITY_TO_MODEL -> {
                if (!valuesDiffer) return false
                if (entityValue != null) {
                    dataProperty.set(dtoModel, entityValue)
                    updated(dataProperty.name, propertyType, UpdateMode.MODEL_TO_ENTITY_FORCED)
                    true
                }
                false
            }

            UpdateMode.ENTITY_TO_MODEL_FORCED -> {
                if (entityValue != null) {
                    dataProperty.set(dtoModel, entityValue)
                    updated(dataProperty.name, propertyType, UpdateMode.MODEL_TO_ENTITY_FORCED)
                }
                true
            }

            UpdateMode.MODEL_TO_ENTITY -> {
                if (!valuesDiffer) {
                    false
                } else {
                    entityProperty.set(entityModel, dtoValue)
                    true
                }
            }

            UpdateMode.MODEL_TO_ENTITY_FORCED -> {
                if (entityValue != null) {
                    entityProperty.set(entityModel, dtoValue)
                    true
                }
                false
            }
        }

        if(updated){
            updated = false
            onModelUpdatedCallback?.invoke(this)
        }

        return result
    }
}

