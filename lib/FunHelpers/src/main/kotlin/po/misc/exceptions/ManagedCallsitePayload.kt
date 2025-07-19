package po.misc.exceptions

import po.misc.data.helpers.textIfNull
import po.misc.context.CTX
import po.misc.context.Identifiable

class ManagedCallSitePayload(
    val producer: CTX,
    var message: String = "",
    val handler: HandlerType? = null,
    val source: Enum<*>? = null,
    val cause: Throwable? = null
){
    var targetObject: String? = null
    var description: String? = null

    fun message(msg: String):ManagedCallSitePayload{
        message = msg
        return this
    }

    fun valueFailure(parameterName: String, parameterTypeName: String):ManagedCallSitePayload{
        message = "$parameterName : $parameterTypeName"
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

        fun  create(producer: CTX, message: String = ""):ManagedCallSitePayload{
           return ManagedCallSitePayload(producer,  message)
        }
    }
}

fun CTX.toPayload(message: String = ""):ManagedCallSitePayload{
   return ManagedCallSitePayload(this,  message)
}

fun CTX.toPayload(cause: Throwable):ManagedCallSitePayload{
    return ManagedCallSitePayload(this, cause =  cause)
}