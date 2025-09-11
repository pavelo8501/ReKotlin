package po.exposify.scope.launchers

import org.jetbrains.exposed.dao.LongEntity
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.bindings.helpers.withDTOContextCreating
import po.exposify.dto.components.query.WhereQuery
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.lognotify.launchers.runProcess
import po.misc.functions.containers.DeferredContainer


suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity> AuthorizedSession.pick(
    dtoClass: RootDTO<DTO, D, E>,
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

suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity> AuthorizedSession.pick(
    dtoClass:RootDTO<DTO, D, E>,
    query: DeferredContainer<WhereQuery<E>>
): ResultSingle<DTO, D> {
    return runProcess(this){
        val emitter = dtoClass.serviceContext.serviceClass.requestEmitter(it)
        emitter.dispatch {
            dtoClass.executionContext.pick(query.resolve())
        }
    }
}

suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity, R> AuthorizedSession.pick(
    dtoClass:RootDTO<DTO, D, E>,
    query: DeferredContainer<WhereQuery<E>>,
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


fun <DTO: ModelDTO, D: DataModel, E: LongEntity,F: ModelDTO , FD: DataModel, R> ResultSingle<F, FD>.pick(
    dtoClass:DTOClass<DTO, D, E>,
    id: Long,
    block:ResultSingle<DTO, D>.()-> R
): R {

    getAsCommonDTO().withDTOContextCreating(dtoClass){
        val pickResult = pick(id)
        val blockResult =   pickResult.block()
        return  blockResult
    }

}




fun <DTO: ModelDTO, D: DataModel, E: LongEntity, F: ModelDTO, FD: DataModel> ResultSingle<F, FD>.pick(
    dtoClass: DTOClass<DTO, D, E>,
    id: Long,
): ResultSingle<DTO, D>{

   return getAsCommonDTO().withDTOContextCreating(dtoClass){
        pick(id)
    }
}



fun <DTO: ModelDTO, D: DataModel, E: LongEntity, F: ModelDTO, FD: DataModel> ResultSingle<F, FD>.pick(
    dtoClass: DTOClass<DTO, D, E>,
    whereQuery: DeferredContainer<WhereQuery<E>>
): ResultSingle<DTO, D>{
    return getAsCommonDTO().withDTOContextCreating(dtoClass){
        pick(whereQuery.resolve())
    }
}

