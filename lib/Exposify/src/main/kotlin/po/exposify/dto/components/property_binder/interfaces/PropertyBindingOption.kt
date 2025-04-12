package po.exposify.dto.components.property_binder.interfaces

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.property_binder.enums.PropertyType
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.entity.classes.ExposifyEntityBase
import kotlin.reflect.KProperty1


interface PropertyBindingOption<DATA : DataModel, ENT : ExposifyEntityBase, T>{
    val dataProperty:KProperty1<DATA, T>
    //  val entityProperty:KProperty1<ENT, T>
    val propertyType: PropertyType

    val dataPropertyName: String
        get() = dataProperty.name

    fun onModelUpdated(callback: (property : PropertyBindingOption<DATA, ENT, T>)-> Unit)
    fun onPropertyUpdated(callback: (name : String, type:PropertyType, updateMode : UpdateMode)-> Unit)
    fun updated(name : String,  type:PropertyType, updateMode : UpdateMode)
}