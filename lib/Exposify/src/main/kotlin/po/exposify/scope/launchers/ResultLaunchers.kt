package po.exposify.scope.launchers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOClass
import po.exposify.dto.components.bindings.helpers.withDTOContextCreating
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.functions.containers.DeferredContainer


fun <DTO: ModelDTO, D: DataModel,  E: LongEntity, F: ModelDTO, FD: DataModel> ResultSingle<F, FD>.pick(
    dtoClass: DTOClass<DTO, D, E>,
    id: Long
): ResultSingle<DTO, D>{
    TODO("Part of refactor")
//    return withDTOContextCreating(getAsCommonDTO(), dtoClass){
//        pick(id)
//    }
}

fun <DTO: ModelDTO, D: DataModel, E: LongEntity, F: ModelDTO, FD: DataModel> ResultSingle<F, FD>.pick(
    dtoClass: DTOClass<DTO, D, E>,
    whereQuery: DeferredContainer<WhereQuery<E>>
): ResultSingle<DTO, D>{
    TODO("Part of refactor")
//    return withDTOContextCreating(getAsCommonDTO(), dtoClass){
//        pick(whereQuery.resolve())
//    }
}

