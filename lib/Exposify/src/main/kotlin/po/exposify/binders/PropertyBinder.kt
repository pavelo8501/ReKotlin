package po.exposify.binders

import kotlinx.serialization.KSerializer
import org.jetbrains.exposed.dao.LongEntity
import po.exposify.classes.interfaces.DataModel
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


enum class UpdateMode{
    ENTITY_TO_MODEL,
    ENTITY_TO_MODEL_FORCED,
    MODEL_TO_ENTITY,
    MODEL_TO_ENTITY_FORCED,
}

enum class PropertyType{
    ONE_WAY,
    TWO_WAY,
    SERIALIZED
}

interface PropertyBindingOption<DATA : DataModel, ENT : LongEntity, T>{
     val dtoProperty:KProperty1<DATA, T>
     val entityProperty:KProperty1<ENT, T>
     val propertyType: PropertyType

     fun onModelUpdated(callback: (property : PropertyBindingOption<DATA,ENT,T>)-> Unit)
     fun onPropertyUpdated(callback: (name : String, type:PropertyType, updateMode : UpdateMode)-> Unit)
     fun updated(name : String,  type:PropertyType, updateMode : UpdateMode)
}

class SyncedSerialized<DATA : DataModel, ENT : LongEntity, C, TYPE: Any >(
    override val dtoProperty:KMutableProperty1<DATA, C>,
    override val entityProperty:KMutableProperty1<ENT, C>,
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
    override fun updated(
        name: String,
        type: PropertyType,
        updateMode: UpdateMode
    ) {
        updated = true
        onPropertyUpdatedCallback?.invoke(name, type, updateMode)
    }

    fun getSerializer(): Pair<String, KSerializer<TYPE> >{
        return Pair(dtoProperty.name, serializer)
    }

    fun update(dtoModel: DATA, entityModel: ENT, mode: UpdateMode): Boolean {
        updated = false
        val dtoValue = dtoProperty.get(dtoModel)

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
                    dtoProperty.set(dtoModel, entityValue)
                    updated(dtoProperty.name, propertyType, UpdateMode.MODEL_TO_ENTITY_FORCED)
                    true
                }
                false
            }

            UpdateMode.ENTITY_TO_MODEL_FORCED -> {
                if (entityValue != null) {
                    dtoProperty.set(dtoModel, entityValue)
                    updated(dtoProperty.name, propertyType, UpdateMode.MODEL_TO_ENTITY_FORCED)
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

class ReadOnly<DATA : DataModel, ENT : LongEntity, T>(
    override val dtoProperty: KMutableProperty1<DATA, T>,
    override val entityProperty: KProperty1<ENT, T>
): PropertyBindingOption<DATA, ENT, T>
{
    override val propertyType: PropertyType = PropertyType.ONE_WAY

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
        val dtoValue = dtoProperty.get(dtoModel)
        val entityValue =  try {
            entityProperty.get(entityModel)
        }catch (ex: Exception){
            null
        }
        val valuesDiffer = dtoValue != entityValue

        return when (mode) {
            UpdateMode.MODEL_TO_ENTITY -> {
                if (!valuesDiffer) return false
                if(entityValue != null){
                    dtoProperty.set(dtoModel, entityValue)
                    updated(dtoProperty.name, propertyType, UpdateMode.MODEL_TO_ENTITY)
                    return true
                }
                return false
            }

            UpdateMode.MODEL_TO_ENTITY_FORCED -> {
                if(entityValue != null) {
                    dtoProperty.set(dtoModel, entityValue)
                    updated(dtoProperty.name, propertyType, UpdateMode.MODEL_TO_ENTITY_FORCED)
                    return true
                }
                return false
            }
            else -> { false  }
        }
        if(updated){
            updated = false
            onModelUpdatedCallback?.invoke(this)
        }
    }
}


class SyncedBinding<DATA : DataModel, ENT : LongEntity, T>(
    override val dtoProperty:KMutableProperty1<DATA, T>,
    override val entityProperty:KMutableProperty1<ENT, T>
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
                    updated(dtoProperty.name, propertyType, UpdateMode.ENTITY_TO_MODEL)
                    return true
                }
                return false
            }
            UpdateMode.ENTITY_TO_MODEL_FORCED -> {
                if(entityValue != null){
                    dtoProperty.set(dtoModel, entityValue)
                    updated(dtoProperty.name, propertyType, UpdateMode.ENTITY_TO_MODEL)
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

class PropertyBinder<DATA : DataModel, ENT : LongEntity>
{
    private var allBindings: List<PropertyBindingOption<DATA, ENT, *>> = listOf()

    var onInitialized: ((PropertyBinder<DATA, ENT>) -> Unit)? = null
    var syncedList: List<SyncedBinding<DATA, ENT, *>> = listOf<SyncedBinding<DATA, ENT, *>> ()
        private set

    var readOnlyPropertyList: List<ReadOnly<DATA, ENT, *>> =  listOf<ReadOnly<DATA, ENT, *>>()
        private set
    var compositePropertyList: List<SyncedSerialized<DATA, ENT, *, *>> =  listOf<SyncedSerialized<DATA, ENT, *, *>>()
        private set

    fun getAllProperties():List<PropertyBindingOption<DATA, ENT, *>>{
        return  allBindings
    }

    fun setProperties(properties: List<PropertyBindingOption<DATA, ENT, *>>) {
        allBindings = properties
        val readOnly = mutableListOf<ReadOnly<DATA, ENT, *>>()
        val synced = mutableListOf<SyncedBinding<DATA, ENT, *>>()
        val compositeList = mutableListOf<SyncedSerialized<DATA, ENT, *, *>>()
        properties.forEach {
            when(it.propertyType){
                PropertyType.ONE_WAY -> {
                    readOnly.add(it as ReadOnly<DATA, ENT, *>)
                }
                PropertyType.TWO_WAY -> {
                  synced.add(it as SyncedBinding<DATA, ENT, *>)
                }
                PropertyType.SERIALIZED -> {
                  compositeList.add(it as SyncedSerialized<DATA, ENT, *, *>)
                }
            }
        }
        readOnlyPropertyList = readOnly
        compositePropertyList = compositeList.toList()
        syncedList = synced.toList()
        onInitialized?.invoke(this)
    }

    fun update(dataModel: DATA, daoModel: ENT, updateMode: UpdateMode) {
        try {
            syncedList.forEach { it.update(dataModel, daoModel, updateMode) }
            compositePropertyList.forEach { it.update(dataModel, daoModel, updateMode) }
            readOnlyPropertyList.forEach { it.update(dataModel, daoModel, updateMode) }
        }catch (ex: Exception){
            println("Property Binder: ${ex.message}")
            throw ex
        }
    }
}