package po.exposify.dto.components

import po.exposify.binders.PropertyBinder
import po.exposify.binders.PropertyType
import po.exposify.binders.UpdateMode
import po.exposify.binders.relationship.models.PropertyInfo
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.classes.ClassBlueprint
import po.exposify.dto.interfaces.ModelDTO
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible


class DataModelContainer2<DTO : ModelDTO, DATA: DataModel>(
    internal val dataModel: DATA,
    val dataBlueprint: ClassBlueprint<DATA>,
    private var binder: PropertyBinder<DATA, *>? = null
) {

    val trackedProperties: MutableMap<String, PropertyInfo<DTO, DATA>> = mutableMapOf()

    operator fun getValue(thisRef: Any, property: KProperty<*>): MutableList<DATA> {
        property.getter.let { getter ->
            getter.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            return getter.call(thisRef) as MutableList<DATA>
        }
    }

    fun binderUpdatedProperty(name : String, type : PropertyType, updateMode : UpdateMode){
        println("BinderUpdatedProperty callback triggered by $name")
    }

    fun setTrackedProperties(list: List<PropertyInfo<DTO, DATA>>){
        list.forEach {propertyInfo->
            dataBlueprint.propertyMap[propertyInfo.name]?.let {blueprintProperty->
                propertyInfo.inBlueprint = blueprintProperty
            }
            trackedProperties[propertyInfo.name] = propertyInfo
        }
    }

    fun attachBinder(propertyBinder: PropertyBinder<DATA, *>){
        binder = propertyBinder
        propertyBinder.syncedList.forEach {
            it.onPropertyUpdated(::binderUpdatedProperty)
        }
    }

//    fun <PARENT_DATA: DataModel>extractChildModels(
//        forPropertyInfo : PropertyInfo<DTO, DATA>
//    ): List<DATA>{
//        try {
//            return  property.get(owningDataModel)
//        }catch (ex: IllegalStateException){
//            println(ex.message)
//            return null
//        }
//    }


    fun <PARENT_DATA: DataModel>extractChildModels(

        property: KProperty1<PARENT_DATA, DATA?>,
        owningDataModel:PARENT_DATA): DATA?{
        try {
            return  property.get(owningDataModel)
        }catch (ex: IllegalStateException){
            println(ex.message)
            return null
        }
    }

    fun <CHILD_DATA: DataModel>addToMutableProperty(name: String, value: CHILD_DATA){
        dataBlueprint.propertyMap[name]?.let {
            @Suppress("UNCHECKED_CAST")
            it as KProperty1<DATA, MutableList<CHILD_DATA>>
            it.isAccessible = true
            it.get(dataModel).add(value)
        }
    }

    fun <CHILD_DATA: DataModel>setProperty(name: String, value: CHILD_DATA){

        dataBlueprint.propertyMap[name]?.let {
            @Suppress("UNCHECKED_CAST")
            it as KMutableProperty1<DATA, CHILD_DATA>
            it.isAccessible = true
            it.set(dataModel, value)
        }
    }

}