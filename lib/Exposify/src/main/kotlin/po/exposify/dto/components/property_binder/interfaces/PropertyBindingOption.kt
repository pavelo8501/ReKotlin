package po.exposify.dto.components.property_binder.interfaces

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.components.property_binder.enums.PropertyType
import kotlin.reflect.KProperty1


interface PropertyBindingOption<DATA : DataModel, ENT : LongEntity, T>{
    val dataProperty:KProperty1<DATA, T>
    val referencedProperty:KProperty1<ENT, *>
    val propertyType: PropertyType

}