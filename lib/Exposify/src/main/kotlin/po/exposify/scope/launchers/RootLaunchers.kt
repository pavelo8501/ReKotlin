package po.exposify.scope.launchers

import org.jetbrains.exposed.dao.LongEntity
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.RootDTO
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



suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity>  AuthorizedSession.update(
    dtoClass:RootDTO<DTO, D, E>,
    list: List<D>
): ResultList<DTO, D>{

    return runProcess(this) {
        val emitter = dtoClass.serviceContext.serviceClass.requestEmitter(it)
        emitter.dispatchList {
            dtoClass.executionContext.update(list)
        }
    }
}

suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity>  AuthorizedSession.update(
    dtoClass:RootDTO<DTO, D, E>,
    dataModel: D
): ResultSingle<DTO, D> = runProcess(this) {
    val emitter = dtoClass.serviceContext.serviceClass.requestEmitter(it)
     emitter.dispatchSingle {
        dtoClass.executionContext.update(dataModel)
    }
}

suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity, R>  AuthorizedSession.update(
    dtoClass:RootDTO<DTO, D, E>,
    dataModel: D,
    block:ResultSingle<DTO, D>.()-> R
): R {
    val session = this
   return runProcess(this) {
        val emitter = dtoClass.serviceContext.serviceClass.requestEmitter(it)
        val result =  emitter.dispatch {
            val updateResult =  dtoClass.executionContext.update(dataModel)
            updateResult.saveSession(session)
            updateResult.block()
        }
        result
    }
}

suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity> AuthorizedSession.pick(
    dtoClass:RootDTO<DTO, D, E>,
    id: Long
): ResultSingle<DTO, D>{
    val session = this
    return runProcess(session){
        val emitter = dtoClass.serviceContext.serviceClass.requestEmitter(it)
        emitter.dispatchSingle {
            dtoClass.executionContext.pick(id)
        }
    }
}


suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity, R> AuthorizedSession.pick(
    dtoClass:RootDTO<DTO, D, E>,
    id: Long,
    block:ResultSingle<DTO, D>.()-> R
): R {
    val session = this
    val result = runProcess(this){
        val emitter = dtoClass.serviceContext.serviceClass.requestEmitter(it)
        emitter.dispatch {
            val pickResult =  dtoClass.executionContext.pick(id)
            pickResult.saveSession(session)
            pickResult.block()
        }
    }
    return result
}

suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity> AuthorizedSession.pick(
    dtoClass:RootDTO<DTO, D, E>,
    query: DeferredContainer<WhereQuery<*>>,
): ResultSingle<DTO, D>{
    val session = this
    return runProcess(this){
        val emitter = dtoClass.serviceContext.serviceClass.requestEmitter(it)
        emitter.dispatchSingle {
            dtoClass.executionContext.pick(query.resolve())
        }
    }
}

suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity, R> AuthorizedSession.pick(
    dtoClass:RootDTO<DTO, D, E>,
    query: DeferredContainer<WhereQuery<*>>,
    block:ResultSingle<DTO, D>.()-> R
):R{
    val session = this
    val result = runProcess(session) {
        val emitter = dtoClass.serviceContext.serviceClass.requestEmitter(it)
        emitter.dispatch {
            val pickResult = dtoClass.executionContext.pick(query.resolve())
            pickResult.saveSession(session)
            pickResult.block()
        }
    }
    return result
}
