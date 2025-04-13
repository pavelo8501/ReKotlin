package po.exposify.dto.components.property_binder.interfaces

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.property_binder.enums.PropertyType
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.entity.classes.ExposifyEntityBase
import kotlin.reflect.KProperty1


interface PropertyBindingOption<DATA : DataModel, ENT : ExposifyEntityBase, T>{
    val dataProperty:KProperty1<DATA, T>
    val referencedProperty:KProperty1<ENT, *>
    val propertyType: PropertyType

    val dataPropertyName: String
        get() = dataProperty.name

    val referencedPropertyName: String
        get() = referencedProperty.name


    var onDataUpdatedCallback: ((PropertyBindingOption<DATA, ENT, T>) -> Unit)?
    fun  setDataUpdatedUpdated(callback : (PropertyBindingOption<DATA, ENT, T>) -> Unit){
        onDataUpdatedCallback = callback
    }
    fun dataUpdatedUpdated(){
        onDataUpdatedCallback?.invoke(this)
    }

    fun onPropertyUpdated(callback: (name : String, type:PropertyType, updateMode : UpdateMode)-> Unit)
    fun updated(name : String,  type:PropertyType, updateMode : UpdateMode)
}