package po.exposify.dto.components

import po.exposify.binder.PropertyBinder
import po.exposify.classes.interfaces.DataModel
import po.exposify.constructors.DataModelBlueprint
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible


class DataModelContainer<T: DataModel>(
    internal val dataModel: T,
    val dataBlueprint: DataModelBlueprint<T>,
    private var binder: PropertyBinder<T, *>? = null
) {


    //companion object : ConstructorBuilder()
  //  var dataBlueprint = DataModelBlueprint(dataModel::class).also { it.initialize(Companion) }

    init {
       // processDataModel()
    }


    fun attachBinder(propertyBinder: PropertyBinder<T, *>){
        binder = propertyBinder
    }


//    private fun processDataModel(){
//        val dataModelClass = dataModel::class
////        dataBlueprint = DataModelBlueprint(dataModelClass).also {
////            it.initialize(Companion)
////        }
//    }

    operator fun getValue(thisRef: Any, property: KProperty<*>): MutableList<T> {
        property.getter.let { getter ->
            getter.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            return getter.call(thisRef) as MutableList<T>
        }
    }


//    operator fun <C: Any> setValue(thisRef: Any, property: KProperty<*>, value: C) {
////        property.getter.let { getter ->
////            getter.isAccessible = true
////            getter.call(thisRef)
////            val list = mutableListOf<C>()
////            list.add(value)
////        }
//
//        val a = 10
//
//    }


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