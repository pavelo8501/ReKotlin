package po.exposify.scope.launchers

import org.jetbrains.exposed.dao.LongEntity
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.bindings.helpers.withDTOContextCreating
import po.exposify.dto.components.result.ResultList
import po.exposify.dto.components.result.ResultSingle
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.lognotify.launchers.runProcess


suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity>  AuthorizedSession.insert(
    dtoClass:RootDTO<DTO, D, E>,
    dataModels: List<D>
): ResultList<DTO, D>{

    return runProcess(this){
        val emitter = dtoClass.serviceContext.serviceClass.requestEmitter(it)
        emitter.dispatchList {
            dtoClass.executionContext.insert(dataModels)
        }
    }
}

suspend fun <DTO: ModelDTO, D: DataModel, E: LongEntity>  AuthorizedSession.insert(
    dtoClass:RootDTO<DTO, D, E>,
    dataModel:D
): ResultSingle<DTO, D>{

    return runProcess(this) {
        val emitter = dtoClass.serviceContext.serviceClass.requestEmitter(it)
        emitter.dispatchSingle {
            dtoClass.executionContext.insert(dataModel)
        }
    }
}

fun <DTO: ModelDTO, D: DataModel, E: LongEntity, F: ModelDTO, FD: DataModel> ResultSingle<F, FD>.insert(
    dtoClass: DTOClass<DTO, D, E>,
    dataModels: List<D>
): ResultList<DTO, D>{
    return  getAsCommonDTO().withDTOContextCreating(dtoClass){
        insert(this,  dataModels)
    }
}

fun <DTO: ModelDTO, D: DataModel, E: LongEntity, F: ModelDTO, FD: DataModel> CommonDTO<F, FD, *>.insert(
    dtoClass: DTOClass<DTO, D, E>,
    dataModels: List<D>
): ResultList<DTO, D>{
    return  withDTOContextCreating(dtoClass){
        insert(this,  dataModels)

    }
}




