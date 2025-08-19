package po.exposify.scope.launchers

import org.jetbrains.exposed.dao.LongEntity
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.bindings.helpers.withDTOContextCreating
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.lognotify.launchers.runProcess


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


fun <DTO: ModelDTO, D: DataModel, E: LongEntity, F: ModelDTO, FD: DataModel> ResultSingle<F, FD>.update(
    dtoClass: DTOClass<DTO, D, E>,
    data: D
): ResultSingle<DTO, D>{

   return getAsCommonDTO().withDTOContextCreating(dtoClass){
        update(this,  data)
    }
}

fun <DTO: ModelDTO, D: DataModel, E: LongEntity, F: ModelDTO, FD: DataModel> ResultSingle<F, FD>.update(
    dtoClass: DTOClass<DTO, D, E>,
    data: List<D>,
): ResultList<DTO, D> {
    return getAsCommonDTO().withDTOContextCreating(dtoClass){
        update(this,  data)
    }
}

