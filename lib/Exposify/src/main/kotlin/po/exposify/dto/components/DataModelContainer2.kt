package po.exposify.dto.components

import po.exposify.binder.PropertyBinder
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.classes.ClassBlueprint
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible


class DataModelContainer2<T: DataModel>(
    internal val dataModel: T,
    val dataBlueprint: ClassBlueprint<T>,
    private var binder: PropertyBinder<T, *>? = null
) {
    fun attachBinder(propertyBinder: PropertyBinder<T, *>){
        binder = propertyBinder
    }

    operator fun getValue(thisRef: Any, property: KProperty<*>): MutableList<T> {
        property.getter.let { getter ->
            getter.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            return getter.call(thisRef) as MutableList<T>
        }
    }

    fun <CHILD_DATA: DataModel>addToMutableProperty(name: String, value: CHILD_DATA){
        dataBlueprint.propertyMap[name]?.let {
            @Suppress("UNCHECKED_CAST")
            it as KProperty1<T, MutableList<CHILD_DATA>>
            it.isAccessible = true
            it.get(dataModel).add(value)
        }
    }

    fun <CHILD_DATA: DataModel>setProperty(name: String, value: CHILD_DATA){

        dataBlueprint.propertyMap[name]?.let {
            @Suppress("UNCHECKED_CAST")
            it as KMutableProperty1<T, CHILD_DATA>
            it.isAccessible = true
            it.set(dataModel, value)
        }
    }

}