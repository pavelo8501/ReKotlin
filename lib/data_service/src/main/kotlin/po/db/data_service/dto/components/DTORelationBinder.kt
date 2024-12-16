package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import kotlin.reflect.KClass

enum class BindingType{
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_ONE,
    MANY_TO_MANY
}


class DTORelationBindingContainer<C_DAT, C_ENT>(
    val dtoModelClass : CommonDTO<C_DAT, C_ENT>,
    val type:  BindingType
) where C_DAT : DataModel, C_ENT : LongEntity{

}

class DTORelationBinder<DATA_MODEL, ENTITY>(
    val parentDtoModel : DTOClass<DATA_MODEL, ENTITY>
) where DATA_MODEL:DataModel, ENTITY : LongEntity {

    var childBindings = mutableMapOf<String, DTORelationBindingContainer<*, *>>()

    inline fun <reified CHILD_DTO : CommonDTO<C_DAT, C_ENT>, reified C_DAT, reified C_ENT>  addChildBinding(dtoClass: CommonDTO<C_DAT, C_ENT>, type:  BindingType) where C_DAT: DataModel,C_ENT : LongEntity {
        DTORelationBindingContainer<C_DAT ,C_ENT>(dtoClass,type).let { this.childBindings.putIfAbsent("TempName" ,it) }
    }

}