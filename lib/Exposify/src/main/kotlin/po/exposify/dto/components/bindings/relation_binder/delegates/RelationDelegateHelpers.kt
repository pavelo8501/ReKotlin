package po.exposify.dto.components.bindings.relation_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO


inline fun <DTO, D, E, F, FD, FE>  List<RelationDelegate<DTO, D, E, F, FD, FE, *>>.withEachDelegate(
    block:RelationDelegate<DTO, D, E, F, FD, FE, *>.(dtoClass: DTOBase<F, FD, FE>)-> Unit
) where DTO: ModelDTO, D: DataModel, E : LongEntity, F : ModelDTO , FD: DataModel, FE: LongEntity {
    forEach {delegate->
        block.invoke(delegate, delegate.dtoClass)
    }
}