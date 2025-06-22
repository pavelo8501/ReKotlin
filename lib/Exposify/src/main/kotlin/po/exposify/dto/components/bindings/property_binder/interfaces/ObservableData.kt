package po.exposify.dto.components.bindings.property_binder.interfaces

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased

interface ObservableData : Identifiable{
    val id: Long
    val operation : CrudOperation
    val methodName: String
    val propertyName: String
    val oldValue: Any?
    val newValue: Any
}