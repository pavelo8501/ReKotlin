package po.exposify.scope.sequence.extensions

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.SwitchQuery
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.classes.RootHandlerProvider
import po.exposify.scope.sequence.classes.SwitchHandlerProvider
import po.exposify.scope.sequence.models.HandlerConfig


//This executed for root dto object. Parent/Owner runs query
suspend fun <DTO: ModelDTO, D: DataModel, E : LongEntity> runSequence(
    handlerDelegate: RootHandlerProvider<DTO, D, E>,
    configBuilder:  suspend HandlerConfig<DTO, D, E>.()-> Unit
): ResultList<DTO, D, E>{

    val newHandler = handlerDelegate.createHandler()
    newHandler.provideConfigFn(configBuilder)
    val emitter = newHandler.dtoRoot.getServiceClass().requestEmitter()
    return emitter.dispatchRoot(newHandler)
}

//This executed for child dto object. Switch query is immediately asked in the constructor. Child runs query
suspend fun <DTO: ModelDTO, D: DataModel, E : LongEntity, F_DTO: ModelDTO, FD : DataModel,  FE: LongEntity> runSequence(
    handlerDelegate: SwitchHandlerProvider<DTO, D, E, F_DTO, FD, FE>,
    switchQuery: ()->SwitchQuery<F_DTO, FD, FE>,
    configBuilder: suspend HandlerConfig<DTO, D, E>.()-> Unit
): ResultList<DTO, D, E> {

    val newHandler = handlerDelegate.createHandler()
    configBuilder.invoke(newHandler.handlerConfig)
    val emitter = newHandler.parentHandler.dtoRoot.getServiceClass().requestEmitter()
    val result =  emitter.dispatchChild(newHandler)
    return newHandler.finalResult
}

suspend fun <T: HandlerConfig<F_DTO, FD, FE>,F_DTO:ModelDTO, FD : DataModel, FE : LongEntity, DTO: ModelDTO, D: DataModel, E: LongEntity> T.usingConfig(
    handlerDelegate : RootHandlerProvider<DTO, D, E>,
    configBuilder: suspend HandlerConfig<DTO, D, E>.()-> Unit
): ResultList<DTO,D, E>{

    val newHandler = handlerDelegate.createHandler()
    newHandler.provideConfigFn(configBuilder)
    val emitter = newHandler.dtoRoot.getServiceClass().requestEmitter()
    return emitter.dispatchRoot(newHandler)
}