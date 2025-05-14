package po.exposify.scope.sequence.extensions

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.SwitchQuery
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.classes.Handler
import po.exposify.scope.sequence.classes.SwitchHandler
import po.exposify.scope.sequence.models.HandlerConfig


suspend fun <DTO: ModelDTO, D: DataModel, E : LongEntity> runSequence(
    handler: Handler<DTO, D, E>,
    configBuilder:  suspend HandlerConfig<DTO, D, E>.()-> Unit
): ResultList<DTO, D, E>{

    handler.provideConfigFn(configBuilder)
    val emitter = handler.dtoRoot.getServiceClass().requestEmitter()
    return emitter.smartDispatch(handler)
}


suspend fun <DTO: ModelDTO, D: DataModel, E : LongEntity, F_DTO: ModelDTO, FD : DataModel,  FE: LongEntity> runSequence(
    handler: SwitchHandler<DTO, D, E, F_DTO, FD, FE>,
    switchQuery: SwitchQuery< F_DTO, FD, FE>,
    configBuilder: suspend HandlerConfig<DTO, D, E>.()-> Unit
): ResultList<DTO, D, E> {

    handler.provideConfigFn<DTO, D, E>("test", configBuilder)
    handler.parentHandler.handlerConfig.withQuery(switchQuery)
    val emitter = handler.parentHandler.dtoRoot.getServiceClass().requestEmitter()
    val result =  emitter.smartDispatch(handler)
    return handler.finalResult
}