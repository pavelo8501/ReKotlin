package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.interfaces.DataModel
import kotlin.reflect.KMutableProperty1


enum class UpdateMode{
    ENTITY_TO_MODEL,
    ENTITY_TO_MODEL_FORCED,
    MODEL_TO_ENTITY,
    MODEL_TO_ENTITY_FORCED,
}


sealed class PropertyBindingSealed{
    abstract val name: String
    abstract fun update(dtoModel: DataModel, entityModel: LongEntity, mode: UpdateMode): Boolean
}


class PropertyBinding<DM : DataModel, E : LongEntity, T>(
    override val name: String,
    private val dtoProperty: KMutableProperty1<DM, T>,
    private val entityProperty: KMutableProperty1<E, T>
) : PropertyBindingSealed() {


    override fun update(dtoModel: DataModel, entityModel: LongEntity, mode: UpdateMode): Boolean {
        @Suppress("UNCHECKED_CAST")
        val dm = dtoModel as DM
        @Suppress("UNCHECKED_CAST")
        val em = entityModel as E

        val dtoValue = dtoProperty.get(dm)
        val entityValue: T? = try {
            entityProperty.get(em)
        } catch (e: Exception) {
            null
        }
        val valuesDiffer = dtoValue != entityValue

        return when (mode) {
            UpdateMode.ENTITY_TO_MODEL -> {
                if (!valuesDiffer) return false
                if(entityValue != null){
                    dtoProperty.set(dm, entityValue)
                    return true
                }
                return false
            }
            UpdateMode.MODEL_TO_ENTITY -> {
                if (!valuesDiffer) return false
                entityProperty.set(em, dtoValue)
                true
            }
            UpdateMode.MODEL_TO_ENTITY_FORCED -> {
                if(entityValue != null) {
                    dtoProperty.set(dm, entityValue)
                    return true
                }
                return false
            }
            UpdateMode.ENTITY_TO_MODEL_FORCED -> {
                entityProperty.set(em, dtoValue)
                true
            }
        }
    }
}

class PropertyBinder {
    var onInitialized: ((PropertyBinder) -> Unit)? = null
    private var propertyList = emptyList<PropertyBindingSealed>()

    fun setProperties(properties: List<PropertyBindingSealed>) {
        propertyList = properties
        onInitialized?.invoke(this)
    }

    fun updateProperties(dataModel: DataModel, daoModel: LongEntity, updateMode: UpdateMode) {
        propertyList.forEach { it.update(dataModel, daoModel, updateMode) }
    }
}