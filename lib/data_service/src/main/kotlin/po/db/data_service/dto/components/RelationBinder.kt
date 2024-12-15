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


class BindingContainer<DATA_MODEL, ENTITY, C_DAT, C_ENT>(
    val dtoModelClass : KClass<CommonDTO<C_DAT, C_ENT>>,
    val dtoClass :  DTOClass<C_DAT, C_ENT>,
    val type:  BindingType
) where C_DAT : DataModel, C_ENT : LongEntity{

}

class RelationBinder<DATA_MODEL, ENTITY, C_DAT, C_ENT>() where C_DAT : DataModel, C_ENT : LongEntity {

    //var childBindings =  mutableMapOf<KClass<CommonDTO<DATA_MODEL, ENTITY>>>, List<BindingContainer<C_DAT, C_ENT>?>()


//    fun addChildBinding(dtoClass :   DTOClass<C_DAT, C_ENT>,   type:  BindingType){
//        BindingContainer(className,dtoClass,type).let { this.childBindings.putIfAbsent(it.className,it) }
//    }

}