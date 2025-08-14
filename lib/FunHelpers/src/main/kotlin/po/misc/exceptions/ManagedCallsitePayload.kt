package po.misc.exceptions

import po.misc.context.CTX
import po.misc.data.helpers.replaceIfNull

interface ManagedCallSitePayload{
    val message: String
    val code: Enum<*>?
    val handler: HandlerType?
    val cause: Throwable?
    var methodName: String
    var context: CTX?
    var description: String?


    fun setHandler(handler: HandlerType?):ManagedCallSitePayload
    fun setCode(code: Enum<*>?):ManagedCallSitePayload
    fun setCause(cause: Throwable?):ManagedCallSitePayload
}

class ManagedPayload(
    override val message: String,
    override var methodName: String,
):ManagedCallSitePayload{

    override var context: CTX? = null
    override var handler: HandlerType? = null
        private set

    override var code: Enum<*>? = null
        private set

    override var cause: Throwable? = null
        private set

    override var description: String? = null

    var contextNameBacking: String = "N/A"
    val contextName: String = contextNameBacking

    constructor(message: String, methodName: String,  callingContext: Any): this(message = message, methodName = methodName){
        if(callingContext is CTX){
            context = callingContext
        }else{
            contextNameBacking = callingContext::class.qualifiedName?:"N/A"
        }
    }

    override fun setCause(cause: Throwable?):ManagedPayload{
        this.cause = cause
        return this
    }

    override fun setHandler(handler: HandlerType?):ManagedPayload{
        this.handler = handler
        return this
    }

    override fun setCode(code: Enum<*>?):ManagedPayload{
        this.code = code
        return  this
    }

    override fun toString(): String {
        return message.replaceIfNull()
    }
}

fun CTX.toPayload(message: String, methodName: String, block: ManagedPayload.()-> Unit):ManagedPayload{
    val payload = ManagedPayload(message, methodName, this)
    payload.block()
    return payload
}

fun CTX.toPayload(message: String, methodName: String):ManagedPayload{
    val payload = ManagedPayload(message, methodName, this)
    return payload
}

fun CTX.toPayload(methodName: String, cause: Throwable):ManagedPayload{
    val payload = ManagedPayload(cause.throwableToText(), methodName, this)
    return payload
}

