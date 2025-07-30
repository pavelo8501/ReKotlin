package po.exposify.dto.components.query

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.functions.containers.DeferredContainer


fun <E: LongEntity> whereQuery(dtoClass: DTOBase<*, *, E>, builder:WhereQuery<E>.()-> Unit):WhereQuery<E> {
    val query = WhereQuery(dtoClass)
    query.builder()
    return query
}

fun <DTO: ModelDTO, D: DataModel, E: LongEntity> deferredQuery(
    dtoClass: DTOBase<DTO, D, E>,
    builder:WhereQuery<E>.()-> Unit
): DeferredContainer<WhereQuery<E>> {

    return DeferredContainer(dtoClass){
        whereQuery(dtoClass, builder)
    }
}


fun <DTO: ModelDTO, D: DataModel> deferredQuery2(
    dtoClass: DTOBase<DTO, D, *>,
    builder:WhereQuery<*>.()-> Unit
): DeferredContainer<WhereQuery<*>> {

    return DeferredContainer(dtoClass){
        whereQuery(dtoClass, builder)
    }
}

