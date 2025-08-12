package po.exposify.scope.launchers

import org.jetbrains.exposed.dao.LongEntity
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.RootDTO
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO


suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity>  AuthorizedSession.update(
    dtoClass:RootDTO<DTO, D, E>,
    list: List<D>
): ResultList<DTO, D>{
    val emitter = dtoClass.serviceContext.serviceClass.requestEmitter(this)
    return  emitter.dispatchList {
        dtoClass.executionContext.update(list)
    }
}

suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity>  AuthorizedSession.update(
    dtoClass:RootDTO<DTO, D, E>,
    dataModel: D
): ResultSingle<DTO, D>{
    val emitter = dtoClass.serviceContext.serviceClass.requestEmitter(this)
    return  emitter.dispatchSingle {
        dtoClass.executionContext.update(dataModel)
    }
}

suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity, R>  AuthorizedSession.update(
    dtoClass:RootDTO<DTO, D, E>,
    dataModel: D,
    block:ResultSingle<DTO, D>.()-> R
): R {
    val emitter = dtoClass.serviceContext.serviceClass.requestEmitter(this)
    val result = emitter.dispatchSingle {
        dtoClass.executionContext.update(dataModel)
    }
    result.saveSession(this)
    return result.block()
}

suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity> AuthorizedSession.pick(
    dtoClass:RootDTO<DTO, D, E>,
    id: Long
): ResultSingle<DTO, D>{
    val emitter = dtoClass.serviceContext.serviceClass.requestEmitter(this)
    return  emitter.dispatchSingle {
        dtoClass.executionContext.pick(id)
    }
}


suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity, R> AuthorizedSession.pick(
    dtoClass:RootDTO<DTO, D, E>,
    id: Long,
    block:ResultSingle<DTO, D>.()-> R
): R {
    val emitter = dtoClass.serviceContext.serviceClass.requestEmitter(this)
    val result = emitter.dispatchSingle {
        dtoClass.executionContext.pick(id)
    }
    result.saveSession(this)
    return result.block()
}
