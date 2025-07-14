package po.misc.exceptions

import po.misc.data.helpers.textIfNull
import po.misc.interfaces.IdentifiableContext


class ManagedCallSitePayload(
    val ctx: IdentifiableContext,
    var message: String? = null,
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


    fun method(methodName: String, expectedResult: String):ManagedCallSitePayload{
        message =  "$methodName:$expectedResult"
        return this
    }

    fun provideDescription(methodName: String, reason: String, result: String){
        description +=  "In method: $methodName. Reason:${reason}. Result:${result}"
    }

    override fun toString(): String {
        return message.textIfNull("")
    }

    companion object{
        fun create(message: String, ctx: IdentifiableContext):ManagedCallSitePayload{
           return ManagedCallSitePayload(ctx, message)
        }
    }
}

fun IdentifiableContext.toPayload(message: String):ManagedCallSitePayload{
   return ManagedCallSitePayload(this, message)
}

fun IdentifiableContext.toPayload(cause: Throwable, message: String? = null):ManagedCallSitePayload{
    return ManagedCallSitePayload(this, cause =  cause, message = message?:cause.text())
}