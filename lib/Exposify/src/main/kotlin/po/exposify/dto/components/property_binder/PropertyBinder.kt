package po.exposify.dto.components.property_binder

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.property_binder.bindings.ReadOnlyBinding
import po.exposify.dto.components.property_binder.bindings.SerializedBinding
import po.exposify.dto.components.property_binder.bindings.SyncedBinding
import po.exposify.dto.components.property_binder.enums.PropertyType
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.property_binder.interfaces.PropertyBindingOption
import po.exposify.entity.classes.ExposifyEntityBase

class PropertyBinder<DATA : DataModel, ENT : ExposifyEntityBase>(
    var  onPropertyUpdate : (suspend (String, PropertyType, UpdateMode) -> Unit)?,
    private val onSyncedSerializedAdd : (syncedSerializedProperty:  List<SerializedBinding<DATA, ENT, *, *>>)-> Unit)
{
    private var allBindings: List<PropertyBindingOption<DATA, ENT, *>> = listOf()
    var onInitialized: ( (PropertyBinder<DATA, ENT>) -> Unit)? = null
    var syncedPropertyList: List<SyncedBinding<DATA, ENT, *>> = listOf<SyncedBinding<DATA, ENT, *>> ()
        private set
    var syncedSerializedPropertyList: List<SerializedBinding<DATA, ENT, *, *>> =  listOf<SerializedBinding<DATA, ENT, *, *>>()
        private set

    var readOnlyPropertyList: List<ReadOnlyBinding<DATA, ENT, *>> =  listOf<ReadOnlyBinding<DATA, ENT, *>>()
        private set

//    var referencedPropertyList: List<ReferencedBindingDepr<DATA, ENT>> =  listOf<ReferencedBindingDepr<DATA, ENT>>()
//        private set

    fun getAllProperties():List<PropertyBindingOption<DATA, ENT, *>>{
        return  allBindings
    }

    fun setProperties(properties: List<PropertyBindingOption<DATA, ENT, *>>) {
        allBindings = properties
        val readOnlyList = mutableListOf<ReadOnlyBinding<DATA, ENT, *>>()
        val syncedList = mutableListOf<SyncedBinding<DATA, ENT, *>>()
        val syncedSerializedList = mutableListOf<SerializedBinding<DATA, ENT, *, *>>()
      //  val referencedList = mutableListOf<ReferencedBindingDepr<DATA, ENT>>()
        properties.forEach {
            when(it.propertyType){
                PropertyType.READONLY -> {
                    readOnlyList.add(it as ReadOnlyBinding<DATA, ENT, *>)
                }
                PropertyType.TWO_WAY -> {
                    syncedList.add(it as SyncedBinding<DATA, ENT, *>)
                }
                PropertyType.SERIALIZED -> {
                    syncedSerializedList.add(it as SerializedBinding<DATA, ENT, *, *>)
                }
//                PropertyType.REFERENCED -> {
//                    referencedList.add(it as ReferencedBindingDepr<DATA, ENT>)
//                }
            }
        }
        readOnlyPropertyList = readOnlyList
        syncedSerializedPropertyList = syncedSerializedList.toList()
        if(syncedSerializedPropertyList.count() > 0){
            onSyncedSerializedAdd.invoke(syncedSerializedPropertyList)
        }

     //   referencedPropertyList = referencedList.toList()

        syncedPropertyList = syncedList.toList()
        onInitialized?.invoke(this)
    }

    private var hookFn :  (suspend (DATA, ENT, UpdateMode)-> Unit)? = null
    fun  onUpdate(hook :  suspend (DATA, ENT, UpdateMode)-> Unit){
        hookFn = hook
    }


   suspend fun update(dataModel: DATA, daoModel: ENT, updateMode: UpdateMode) {
       hookFn?.invoke(dataModel, daoModel, updateMode)

            syncedPropertyList.forEach { it.update(dataModel, daoModel, updateMode, onPropertyUpdate) }

            syncedSerializedPropertyList.forEach { it.update(dataModel, daoModel, updateMode, onPropertyUpdate) }
            readOnlyPropertyList.forEach { it.update(dataModel, daoModel, updateMode, onPropertyUpdate) }
//            referencedPropertyList.forEach {
//                if(updateMode == UpdateMode.ENTITY_TO_MODEL){
//                    it.update(dataModel, daoModel, updateMode, onPropertyUpdate)
//                }
//            }
    }
}