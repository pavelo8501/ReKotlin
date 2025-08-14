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
    return withDTOContextCreating(getAsCommonDTO(), dtoClass){
        pick(id)
    }
}

fun <DTO: ModelDTO, D: DataModel, E: LongEntity, F: ModelDTO, FD: DataModel> ResultSingle<F, FD>.pick(
    dtoClass: DTOClass<DTO, D, E>,
    whereQuery: DeferredContainer<WhereQuery<E>>
): ResultSingle<DTO, D>{
    return withDTOContextCreating(getAsCommonDTO(), dtoClass){
        pick(whereQuery.resolve())
    }
}

fun <DTO: ModelDTO, D: DataModel, E: LongEntity, F: ModelDTO, FD: DataModel> ResultSingle<F, FD>.select(
    dtoClass: DTOClass<DTO, D, E>
): ResultList<DTO, D>{

   return withDTOContextCreating(getAsCommonDTO(), dtoClass){

        select()
    }
}

fun <DTO: ModelDTO, D: DataModel, E: LongEntity, F: ModelDTO, FD: DataModel> ResultSingle<F, FD>.select(
    dtoClass: DTOClass<DTO, D, E>,
    whereQuery: DeferredContainer<WhereQuery<E>>,
): ResultList<DTO, D>{
    return withDTOContextCreating(getAsCommonDTO(), dtoClass){
        select(whereQuery.resolve())
    }
}

fun <DTO: ModelDTO, D: DataModel, F: ModelDTO, FD: DataModel, FE: LongEntity> ResultSingle<F, FD>.update(
    dtoClass: DTOClass<DTO, D, FE>,
    dataList: List<D>
): ResultList<DTO, D>{
    return withDTOContextCreating(getAsCommonDTO(), dtoClass){
        update(dataList, authorizedSession?:dtoClass)
    }
}

fun <DTO: ModelDTO, D: DataModel, E: LongEntity, F: ModelDTO, FD: DataModel> ResultSingle<F, FD>.update(
    dtoClass: DTOClass<DTO, D, E>,
    data: D
): ResultSingle<DTO, D>{
    return withDTOContextCreating(getAsCommonDTO(), dtoClass){
        update(data, authorizedSession?:dtoClass)
    }
}

fun <DTO: ModelDTO, D: DataModel, E: LongEntity, F: ModelDTO, FD: DataModel, R> ResultSingle<F, FD>.update(
    dtoClass: DTOClass<DTO, D, E>,
    data: D,
    block:ResultSingle<DTO, D>.()->R
): R {
    val result = withDTOContextCreating(getAsCommonDTO(), dtoClass) {
        update(data, authorizedSession ?: dtoClass)
    }
    result.saveSession(authorizedSession)
    return result.block()
}

