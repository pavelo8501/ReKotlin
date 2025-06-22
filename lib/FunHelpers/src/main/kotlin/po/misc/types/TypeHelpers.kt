package po.misc.types

import po.misc.callbacks.manager.CallbackPayload
import po.misc.data.helpers.emptyOnNull
import po.misc.data.helpers.textIfNull
import po.misc.exceptions.ManagedException
import po.misc.exceptions.ManageableException
import po.misc.exceptions.ManagedCallsitePayload
import po.misc.exceptions.managedException
import po.misc.exceptions.throwManaged
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


inline fun <T1 : Any, R : Any> safeLet(p1: T1?, block: (T1) -> R?): R? {
    return if (p1 != null) block(p1) else null
}

inline fun <reified T : Any, reified E: ManagedException> T?.getOrThrow(
    message: String? = null,
    code: Enum<*> ?  = null
):T {
    if(this == null){
        val effectiveMessage  = message?:"Expected class ${T::class.simpleName} is null"
        throw ManageableException.build<E>(effectiveMessage, code)
    }else{
        return this
    }
}


fun <T : Any> T?.getOrManaged(
    payload: ManagedCallsitePayload
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
    className: String
):T {
    if(this == null){
        val  code: Enum<*>? = null
        val message = "Unable to return object of class: $className. Object is null."
        throw ManageableException.build<ManagedException>(message, code)
    }else{
        return this
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