package po.exposify.dto.components.property_binder.interfaces

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.components.property_binder.enums.PropertyType
import po.exposify.entity.classes.ExposifyEntity
import kotlin.reflect.KProperty1


interface PropertyBindingOption<DATA : DataModel, ENT : ExposifyEntity, T>{
    val dataProperty:KProperty1<DATA, T>
    val referencedProperty:KProperty1<ENT, *>
    val propertyType: PropertyType

}