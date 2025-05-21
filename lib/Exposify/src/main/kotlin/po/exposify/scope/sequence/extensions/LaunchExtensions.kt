package po.exposify.scope.sequence.extensions

import org.jetbrains.exposed.dao.LongEntity
import po.auth.AuthSessionManager
import po.auth.extensions.createDefaultIdentifier
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.components.ResultList
import po.exposify.dto.components.SwitchQuery
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.sequence.classes.RootHandlerProvider
import po.exposify.scope.sequence.classes.SwitchHandlerProvider
import po.exposify.scope.sequence.models.ClassHandlerConfig
import po.exposify.scope.sequence.models.RootHandlerConfig
import kotlin.coroutines.coroutineContext


//This executed for root dto object. Parent/Owner runs query
suspend fun <DTO: ModelDTO, D: DataModel, E : LongEntity> runSequence(
    handlerDelegate: RootHandlerProvider<DTO, D, E>,
    configBuilder: (suspend RootHandlerConfig<DTO, D, E>.()-> Unit)? = null
): ResultList<DTO, D, E>{
    val newHandler = handlerDelegate.createHandler()
    configBuilder?.invoke(newHandler.handlerConfig)
    val session = coroutineContext[AuthorizedSession]?: AuthSessionManager.getOrCreateSession(createDefaultIdentifier())
    val emitter = newHandler.dtoRoot.getServiceClass().requestEmitter(session)
    return emitter.dispatchRoot(newHandler)
}

//This executed for child dto object. Switch query is immediately asked in the constructor. DtoClass runs query
suspend fun <DTO: ModelDTO, D: DataModel, E : LongEntity, F_DTO: ModelDTO, FD : DataModel,  FE: LongEntity> runSequence(
    handlerDelegate: SwitchHandlerProvider<DTO, D, E, F_DTO, FD, FE>,
    switchQueryProvider: ()-> SwitchQuery<F_DTO, FD, FE>,
    configBuilder: (ClassHandlerConfig<DTO, D, E, F_DTO, FD, FE>.()-> Unit)? = null
): ResultList<DTO, D, E> {

    val classHandler = handlerDelegate.createHandler(switchQueryProvider)
    configBuilder?.invoke(classHandler.handlerConfig)
    if(classHandler.handlerConfig.rootHandlerParameter == null){
        val rootHandler = handlerDelegate.createParentHandler()
        classHandler.handlerConfig.rootHandlerParameter = rootHandler
        rootHandler.handlerConfig.registerSwitchHandler(handlerDelegate.name, classHandler)
    }else{
        classHandler.handlerConfig.rootHandler.handlerConfig.registerSwitchHandler(handlerDelegate.name, classHandler)
    }
    val session = coroutineContext[AuthorizedSession]?: AuthSessionManager.getOrCreateSession(createDefaultIdentifier())
    val emitter = classHandler.handlerConfig.rootHandler.dtoRoot.getServiceClass().requestEmitter(session)
    return emitter.dispatchChild(classHandler)
}

fun <DTO, D, E, F_DTO, FD, FE>  ClassHandlerConfig<DTO, D, E, F_DTO, FD, FE>.usingRoot(
    handlerDelegate : RootHandlerProvider<F_DTO, FD, FE>,
    configBuilder: RootHandlerConfig<F_DTO, FD, FE>.()-> Unit
) where  DTO: ModelDTO, D: DataModel, E: LongEntity,
         F_DTO: ModelDTO, FD : DataModel, FE : LongEntity
{
    val newRootHandler = handlerDelegate.createHandler()
    configBuilder.invoke(newRootHandler.handlerConfig)
    registerRootHandler(newRootHandler)
}

fun <DTO, D, E, F_DTO, FD, FE>  RootHandlerConfig<DTO, D, E>.usingSwitch(
    switchHandlerProvider : SwitchHandlerProvider<F_DTO, FD, FE, DTO, D, E>,
    switchQueryProvider : ()-> SwitchQuery<DTO, D, E>,
    configBuilder: ClassHandlerConfig<F_DTO, FD, FE, DTO, D, E>.()-> Unit
) where  DTO: ModelDTO, D: DataModel, E: LongEntity,
         F_DTO: ModelDTO, FD : DataModel, FE : LongEntity
{
    val newSwitchHandler = switchHandlerProvider.createHandler(switchQueryProvider)
    configBuilder.invoke(newSwitchHandler.handlerConfig)
    registerSwitchHandler(switchHandlerProvider.name, newSwitchHandler)
}