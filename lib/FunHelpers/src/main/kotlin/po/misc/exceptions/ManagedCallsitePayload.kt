package po.misc.exceptions

import po.misc.interfaces.IdentifiableContext


class ManagedCallsitePayload(
    val ctx: IdentifiableContext,
    val message: String,
    val handler: HandlerType? = null,
    val source: Enum<*>? = null,
    val cause: Throwable? = null,
    val outputOverride:((ManagedException)-> Unit)? = null
){

    var targetObject: String? = null

    var description: String? = null

    fun provideDescription(methodName: String, reason: String, result: String){
        description +=  "In method: $methodName. Reason:${reason}. Result:${result}"
    }

}