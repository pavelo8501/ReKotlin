package po.exposify.dto.components.bindings.relation_binder.relation_models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.bindings.property_binder.interfaces.ObservableData
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased

data class RelationsUpdateParams<DTO : ModelDTO, D: DataModel, E: LongEntity, V: Any>(
    val dto: CommonDTO<DTO, D, E>,
    override val operation: CrudOperation,
    override val methodName: String,
    override val propertyName: String,
    override val oldValue: V?,
    override val newValue: V,
    val component : Identifiable
) : ObservableData{

    override var sourceName: String
        get() = dto.sourceName
        set(value) {}
    override val contextName: String get()= component.contextName
    override val id: Long get()= dto.id
}