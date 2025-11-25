package po.misc.exceptions

import po.misc.context.CTX
import po.misc.data.helpers.orDefault
import po.misc.data.helpers.replaceIfNull

interface ThrowableCallSitePayload{

    var methodName: String
    val message: String
    val context:  Any
    var code: Enum<*>?
    var handler: HandlerType?
    val cause: Throwable?
    val helperMethodName: Boolean

    fun setHandler(newHandler: HandlerType?):ThrowableCallSitePayload{
        handler = newHandler
        return this
    }
    fun setCode(newCode: Enum<*>?):ThrowableCallSitePayload{
        code = newCode
        return this
    }
    fun setCause(cause: Throwable?):ThrowableCallSitePayload

}

class ExceptionPayload(
    override var message: String,
    override var methodName: String,
    override var helperMethodName: Boolean,
    override val context:  Any,
):ThrowableCallSitePayload{

    override var code: Enum<*>? = null
    override var handler: HandlerType? = null
    override var cause: Throwable? = null

    fun methodName(name:String, helper: Boolean):ExceptionPayload{
        methodName = name
        helperMethodName = helper
        return this
    }


    override fun setCause(cause: Throwable?):ExceptionPayload{
        this.cause = cause
        return this
    }
}


class ManagedPayload(
    override val message: String,
    override var methodName: String,
    override val context:  Any
):ThrowableCallSitePayload{

    override val helperMethodName: Boolean = true
    override var handler: HandlerType? = null

    override var code: Enum<*>? = null

    override var cause: Throwable? = null
        private set

    var contextNameBacking: String = "N/A"
    val contextName: String = contextNameBacking

    init {
        contextNameBacking = if(context is CTX){
            context.identifiedByName
        }else{
            context::class.qualifiedName?:"N/A"
        }
    }
    override fun setCause(cause: Throwable?):ManagedPayload{
        this.cause = cause
        return this
    }
    override fun setHandler(newHandler: HandlerType?):ManagedPayload{
        this.handler = newHandler
        return this
    }
    override fun setCode(newCode: Enum<*>?):ManagedPayload{
        this.code = newCode
        return  this
    }
    override fun toString(): String {
        return message.orDefault()
    }
}