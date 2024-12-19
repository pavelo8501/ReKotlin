package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
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
    val byProperty : SizedIterable<LongEntity>,
    val type:  OrdinanceType
)

class RelationshipBinder  {
    var bindingKeys = mutableListOf<String>()
        private set
    private var childBindings = mutableMapOf<String, ChildContainer>()

    fun loadChildren(key:String?=null){
        if(key == null){
            bindingKeys.forEach {
                childBindings[it]?.byProperty?.forEach { child ->
                    println(child.id)
                }
            }
        }
    }

    fun addChildBinding(dtoClass: DTOClassV2, byProperty : SizedIterable<LongEntity>, type: OrdinanceType) {
        ChildContainer(dtoClass, byProperty, type).let {
            this.childBindings.putIfAbsent(dtoClass.className, it)
            bindingKeys.add(dtoClass.className)
        }
    }
}
