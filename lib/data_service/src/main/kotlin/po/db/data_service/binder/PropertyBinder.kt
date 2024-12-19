package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.interfaces.DataModel
import kotlin.reflect.KMutableProperty1

sealed class PropertyBindingSealed{
    abstract val name: String
    abstract fun update(dtoModel: DataModel, entityModel: LongEntity, mode: UpdateMode): Boolean
}


class PropertyBindingV2<DM : DataModel, E : LongEntity, T>(
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
        val entityValue = entityProperty.get(em)
        val valuesDiffer = dtoValue != entityValue

        return when (mode) {
            UpdateMode.ENTITY_TO_MODEL -> {
                if (!valuesDiffer) return false
                dtoProperty.set(dm, entityValue)
                true
            }
            UpdateMode.MODEL_TO_ENTITY -> {
                if (!valuesDiffer) return false
                entityProperty.set(em, dtoValue)
                true
            }
            UpdateMode.MODEL_TO_ENTITY_FORCED -> {
                dtoProperty.set(dm, entityValue)
                true
            }
            UpdateMode.ENTITY_TO_MODEL_FORCED -> {
                entityProperty.set(em, dtoValue)
                true
            }
        }
    }
}

class PropertyBinderV2 {
    var onInitialized: ((PropertyBinderV2) -> Unit)? = null
    private var propertyList = emptyList<PropertyBindingSealed>()

    fun setProperties(properties: List<PropertyBindingSealed>) {
        propertyList = properties
        onInitialized?.invoke(this)
    }

    fun updateProperties(dataModel: DataModel, daoModel: LongEntity, updateMode: UpdateMode) {
        propertyList.forEach { it.update(dataModel, daoModel, updateMode) }
    }
}