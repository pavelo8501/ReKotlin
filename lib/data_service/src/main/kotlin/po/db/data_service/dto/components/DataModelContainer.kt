package po.db.data_service.dto.components

import po.db.data_service.classes.components.Factory
import po.db.data_service.classes.interfaces.DataModel
import po.db.data_service.constructors.ConstructorBuilder
import po.db.data_service.constructors.DataModelBlueprint
import kotlin.properties.Delegates
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible


class DataModelContainer<T: DataModel>(val dataModel: T) {

    companion object : ConstructorBuilder()

    var dataBlueprint = DataModelBlueprint(dataModel::class).also { it.initialize(Companion) }

    init {
        disassembleModel()
    }


    private fun disassembleModel(){
        val dataModelClass = dataModel::class
        dataBlueprint = DataModelBlueprint(dataModelClass).also {
            it.initialize(Companion)
        }
    }

    operator fun getValue(thisRef: Any, property: KProperty<*>): MutableList<T> {
        property.getter.let { getter ->
            getter.isAccessible = true
            return getter.call(thisRef) as MutableList<T>
        }
    }

    operator fun <C: Any> setValue(thisRef: Any, property: KProperty<*>, value: C) {
//        property.getter.let { getter ->
//            getter.isAccessible = true
//            getter.call(thisRef)
//            val list = mutableListOf<C>()
//            list.add(value)
//        }

        val a = 10

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