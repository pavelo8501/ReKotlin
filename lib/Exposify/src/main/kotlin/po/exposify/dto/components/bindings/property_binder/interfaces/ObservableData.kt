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

data class UpdateParams<DTO : ModelDTO, D: DataModel, E: LongEntity, V: Any>(
    val dto: CommonDTO<DTO, D, E>,
    override val operation : CrudOperation,
    override val methodName : String,
    override val propertyName: String,
    override val oldValue: V?,
    override val newValue: V,
    val component : Identifiable,
    override val componentName: String = component.componentName,
    override val completeName: String = component.completeName,
    override val id: Long = dto.id
) : ObservableData