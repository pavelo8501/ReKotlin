package po.exposify.dto.components.property_binder.interfaces

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO


interface ObservableData : IdentifiableComponent{
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
    val component : IdentifiableComponent,
    override val qualifiedName: String = component.qualifiedName,
    override val type : ComponentType = component.type,
    override val id: Long = dto.id
) : ObservableData