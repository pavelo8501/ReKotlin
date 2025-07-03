package po.misc.types

import po.misc.callbacks.manager.CallbackPayload
import po.misc.data.helpers.emptyOnNull
import po.misc.data.helpers.textIfNull
import po.misc.exceptions.ManagedException
import po.misc.exceptions.ManageableException
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.managedException
import po.misc.exceptions.throwManaged
import po.misc.interfaces.IdentifiableContext
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


inline fun <T1 : Any, R : Any> safeLet(p1: T1?, block: (T1) -> R?): R? {
    return if (p1 != null) block(p1) else null
}

inline fun <reified T : Any, reified E: ManagedException> T?.getOrThrow(
    ctx: IdentifiableContext? = null,
    exceptionProvider:(String)->E
):T {
    if(this != null){
        return this
    }else{
        val message = "Expected class ${T::class.simpleName} is null"
        val exception =  exceptionProvider(message)
        if(ctx != null){
            exception.throwSelf(ctx)
        }else{
            throw exception
        }
    }
}


fun <T : Any> T?.getOrManaged(
    payload: ManagedCallSitePayload
):T {
    if(this == null){
        var message = "${payload.message}. getOrManaged returned null. ${payload.targetObject.textIfNull(""){ "Target object: ${it}"}}"
        message += payload.description

        val managed = payload.ctx.managedException(message, payload.source, payload.cause)
        payload.outputOverride?.invoke(managed)?: run {  throwManaged(message, payload.handler, payload.source, payload.cause)}
       throw managed
    }else{
        return this
    }
}


fun <T : Any> T?.getOrManaged(
    className: String,
    ctx: IdentifiableContext? = null
):T {
    if(this != null){
        return this
    }else{
        val message = "Unable to return object of class: $className. Object is null."
        throwManaged(message)
    }
}

inline fun <reified T, reified U> initializeContexts(
    receiverInstance: T,
    paramInstance: U,
    block: T.(U) -> Unit
) {
    receiverInstance.block(paramInstance)
}

inline fun <reified T: Any> T.getType(): KClass<T> {
    return T::class
}

fun Any?.isNull(): Boolean{
    return this == null
}

fun Any?.isNotNull(): Boolean{
    return this != null
}