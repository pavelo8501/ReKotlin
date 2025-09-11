package po.exposify.scope.launchers

import org.jetbrains.exposed.dao.LongEntity
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.bindings.helpers.withDTOContextCreating
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.lognotify.launchers.runProcess
import po.misc.functions.containers.DeferredContainer


suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity>  AuthorizedSession.select(
    dtoClass:RootDTO<DTO, D, E>
): ResultList<DTO, D>{
    return runProcess(this){
        val emitter = dtoClass.serviceContext.serviceClass.requestEmitter(it)
        emitter.dispatchList {
            dtoClass.executionContext.select()
        }
    }
}

suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity>  AuthorizedSession.select(
    dtoClass:RootDTO<DTO, D, E>,
    query: DeferredContainer<WhereQuery<E>>
): ResultList<DTO, D>{
    return runProcess(this) {
        val emitter = dtoClass.serviceContext.serviceClass.requestEmitter(it)
        emitter.dispatchList {
            dtoClass.executionContext.select(query.resolve())
        }
    }
}


fun <DTO: ModelDTO, D: DataModel, E: LongEntity, F: ModelDTO, FD: DataModel> ResultSingle<F, FD>.select(
    dtoClass: DTOClass<DTO, D, E>
): ResultList<DTO, D> {

    return getAsCommonDTO().withDTOContextCreating(dtoClass) {
        select()
    }
}


