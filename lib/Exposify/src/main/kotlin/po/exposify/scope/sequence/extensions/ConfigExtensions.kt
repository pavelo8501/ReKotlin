package po.exposify.scope.sequence.extensions

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.ResultList
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.classes.Handler
import po.exposify.scope.sequence.models.HandlerConfig

suspend fun <T: HandlerConfig<F_DTO, FD, FE>,F_DTO:ModelDTO, FD : DataModel, FE : LongEntity, DTO: ModelDTO, D: DataModel, E: LongEntity> T.usingConfig(
    handler : Handler<DTO, D, E>,
    configBuilder: suspend HandlerConfig<DTO, D, E>.()-> Unit
): ResultList<DTO,D, E>{

    handler.provideConfigFn(configBuilder)
    val emitter = handler.dtoRoot.getServiceClass().requestEmitter()
    return emitter.smartDispatch(handler)
}