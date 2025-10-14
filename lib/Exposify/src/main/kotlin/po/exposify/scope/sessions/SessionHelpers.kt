package po.exposify.scope.sessions

import org.jetbrains.exposed.dao.LongEntity
import po.auth.sessions.models.AuthorizedSession
import po.auth.sessions.models.SessionBase
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.executioncontext.ContextEvents
import po.exposify.dto.components.executioncontext.ContextListEvents
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.functions.registries.SubscriptionPack
import po.misc.functions.registries.buildSubscriptions
import kotlin.coroutines.CoroutineContext


internal fun  CoroutineContext.sessionInScope(): SessionBase?{
    return this[AuthorizedSession]
}

fun <DTO: ModelDTO, D: DataModel, E: LongEntity> SessionBase.withHooks(
    dtoClass: DTOBase<DTO, D, E>,
    builder: SubscriptionPack<CommonDTO<DTO, D, E>>.()-> Unit
): SubscriptionPack<CommonDTO<DTO, D, E>> {
   val builtPack = buildSubscriptions(dtoClass.commonDTOType.dtoType, builder)
   setExternalRef(ContextEvents, builtPack)
   return builtPack
}

fun <DTO: ModelDTO, D: DataModel, E: LongEntity> SessionBase.withListHooks(
    dtoClass: DTOBase<DTO, D, E>,
    builder: SubscriptionPack<List<CommonDTO<DTO, D, E>>>.()-> Unit
): SubscriptionPack<List<CommonDTO<DTO, D, E>>> {
    val builtPack = buildSubscriptions(dtoClass.commonDTOType.dtoType, builder)
    setExternalRef(ContextListEvents, builtPack)
    return builtPack
}

