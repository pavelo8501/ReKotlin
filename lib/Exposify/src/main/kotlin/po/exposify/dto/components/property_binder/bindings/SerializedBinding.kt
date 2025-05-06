package po.exposify.dto.components.property_binder.bindings

import kotlinx.serialization.KSerializer
import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.components.property_binder.enums.PropertyType
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.property_binder.interfaces.PropertyBindingOption
import kotlin.reflect.KMutableProperty1

class SerializedBinding<DATA : DataModel, ENT : LongEntity, C, TYPE: Any >(
    override val dataProperty:KMutableProperty1<DATA, C>,
    override val referencedProperty:KMutableProperty1<ENT, C>,
    internal val serializer:  KSerializer<TYPE>,
) : PropertyBindingOption<DATA, ENT, C>
{
    override val propertyType: PropertyType = PropertyType.SERIALIZED

    var updated: Boolean = false

    fun getSerializer(): Pair<String, KSerializer<TYPE>>{
        return Pair(dataProperty.name, serializer)
    }

   fun update(dtoModel: DATA, entityModel: ENT, mode: UpdateMode, callback:  ((String, PropertyType, UpdateMode) -> Unit)?): Boolean {
        updated = false
        val dtoValue = dataProperty.get(dtoModel)

        val entityValue = try {
            referencedProperty.get(entityModel)
        } catch (ex: Exception) {
            null
        }
        val valuesDiffer = dtoValue != entityValue

        val result = when (mode) {
            UpdateMode.ENTITY_TO_MODEL -> {
                if (!valuesDiffer) return false
                if (entityValue != null) {
                    dataProperty.set(dtoModel, entityValue)
                    callback?.invoke(dataProperty.name, propertyType, UpdateMode.MODEL_TO_ENTITY_FORCED)
                    true
                }
                false
            }

            UpdateMode.ENTITY_TO_MODEL_FORCED -> {
                if (entityValue != null) {
                    dataProperty.set(dtoModel, entityValue)
                    callback?.invoke(dataProperty.name, propertyType, UpdateMode.MODEL_TO_ENTITY_FORCED)
                }
                true
            }

            UpdateMode.MODEL_TO_ENTITY -> {
                if (!valuesDiffer) {
                    false
                } else {
                    referencedProperty.set(entityModel, dtoValue)
                    true
                }
            }

            UpdateMode.MODEL_TO_ENTITY_FORCED -> {
                if (entityValue != null) {
                    referencedProperty.set(entityModel, dtoValue)
                    true
                }
                false
            }
        }
        return result
    }
}

