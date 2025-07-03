package po.misc.exceptions

import po.misc.interfaces.IdentifiableContext


class ManagedCallSitePayload(
    val ctx: IdentifiableContext,
    var message: String,
    val handler: HandlerType? = null,
    val source: Enum<*>? = null,
    val cause: Throwable? = null,
    val outputOverride:((ManagedException)-> Unit)? = null
){
    var targetObject: String? = null
    var description: String? = null


    fun message(msg: String):ManagedCallSitePayload{
        message = msg
        return this
    }

    fun provideDescription(methodName: String, reason: String, result: String){
        description +=  "In method: $methodName. Reason:${reason}. Result:${result}"
    }

}