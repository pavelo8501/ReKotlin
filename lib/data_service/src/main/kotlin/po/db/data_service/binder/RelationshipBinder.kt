package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.DTOClassV2
import po.db.data_service.dto.components.BindingType
import po.db.data_service.dto.components.DTORelationBindingContainer
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO


enum class OrdinanceType{
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_ONE,
    MANY_TO_MANY
}

data class ChildContainer(
    val dtoModelClass : DTOClassV2,
    val type:  OrdinanceType
)

class RelationshipBinder  {
    private var childBindings = mutableMapOf<String, ChildContainer>()
    fun addChildBinding(dtoClass: DTOClassV2, type: OrdinanceType) {
        ChildContainer(dtoClass, type).let { this.childBindings.putIfAbsent(dtoClass.className, it) }
    }
}
