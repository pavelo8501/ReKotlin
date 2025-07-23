package po.exposify.dto.components.bindings.relation_binder.relation_models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.context.CTX
import po.misc.context.Identifiable

data class RelationsUpdateParams<DTO : ModelDTO, D: DataModel, E: LongEntity, V: Any>(
    val dto: CommonDTO<DTO, D, E>,
    val operation: CrudOperation,
    val methodName: String,
    val propertyName: String,
    val oldValue: V?,
    val newValue: V,
    val component : CTX
){
    var sourceName: String
        get() = dto.sourceName
        set(value) {}
    val contextName: String get()= component.contextName
    val id: Long get()= dto.id
}