package po.exposify.dto.components.property_binder

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.property_binder.bindings.ReadOnly
import po.exposify.dto.components.property_binder.bindings.ReferencedBinding
import po.exposify.dto.components.property_binder.bindings.SyncedBinding
import po.exposify.dto.components.property_binder.bindings.SyncedSerialized
import po.exposify.dto.components.property_binder.enums.PropertyType
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.property_binder.interfaces.PropertyBindingOption
import po.exposify.entity.classes.ExposifyEntityBase


class PropertyBinder<DATA : DataModel, ENT : ExposifyEntityBase>(

    private val onSyncedSerializedAdd : (syncedSerializedProperty:  List<SyncedSerialized<DATA, ENT, *, *>>)-> Unit)
{
    private var allBindings: List<PropertyBindingOption<DATA, ENT, *>> = listOf()

    var onInitialized: ((PropertyBinder<DATA, ENT>) -> Unit)? = null
    var syncedPropertyList: List<SyncedBinding<DATA, ENT, *>> = listOf<SyncedBinding<DATA, ENT, *>> ()
        private set
    var syncedSerializedPropertyList: List<SyncedSerialized<DATA, ENT, *, *>> =  listOf<SyncedSerialized<DATA, ENT, *, *>>()
        private set

    var readOnlyPropertyList: List<ReadOnly<DATA, ENT, *>> =  listOf<ReadOnly<DATA, ENT, *>>()
        private set

    var referencedPropertyList: List<ReferencedBinding<DATA, ENT>> =  listOf<ReferencedBinding<DATA, ENT>>()
        private set

    fun getAllProperties():List<PropertyBindingOption<DATA, ENT, *>>{
        return  allBindings
    }

    fun setProperties(properties: List<PropertyBindingOption<DATA, ENT, *>>) {
        allBindings = properties
        val readOnlyList = mutableListOf<ReadOnly<DATA, ENT, *>>()
        val syncedList = mutableListOf<SyncedBinding<DATA, ENT, *>>()
        val syncedSerializedList = mutableListOf<SyncedSerialized<DATA, ENT, *, *>>()
        val referencedList = mutableListOf<ReferencedBinding<DATA, ENT>>()
        properties.forEach {
            when(it.propertyType){
                PropertyType.READONLY -> {
                    readOnlyList.add(it as ReadOnly<DATA, ENT, *>)
                }
                PropertyType.TWO_WAY -> {
                    syncedList.add(it as SyncedBinding<DATA, ENT, *>)
                }
                PropertyType.SERIALIZED -> {
                    syncedSerializedList.add(it as SyncedSerialized<DATA, ENT, *, *>)
                }
                PropertyType.REFERENCED -> {
                    referencedList.add(it as ReferencedBinding<DATA, ENT>)
                }
            }
        }
        readOnlyPropertyList = readOnlyList
        syncedSerializedPropertyList = syncedSerializedList.toList()
        if(syncedSerializedPropertyList.count() > 0){
            onSyncedSerializedAdd.invoke(syncedSerializedPropertyList)
        }

        referencedPropertyList = referencedList.toList()

        syncedPropertyList = syncedList.toList()
        onInitialized?.invoke(this)
    }

   suspend fun update(dataModel: DATA, daoModel: ENT, updateMode: UpdateMode) {
        try {
            syncedPropertyList.forEach { it.update(dataModel, daoModel, updateMode) }
            syncedSerializedPropertyList.forEach { it.update(dataModel, daoModel, updateMode) }
            readOnlyPropertyList.forEach { it.update(dataModel, daoModel, updateMode) }
            referencedPropertyList.forEach {
                it.update(dataModel, daoModel, updateMode)
            }
        }catch (ex: Exception){
            println("Property Binder: ${ex.message}")
            throw ex
        }
    }
}