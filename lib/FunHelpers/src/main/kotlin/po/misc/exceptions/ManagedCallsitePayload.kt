package po.misc.exceptions

import po.misc.data.helpers.textIfNull
import po.misc.context.CTX
import po.misc.data.printable.PrintableBase
import po.misc.exceptions.models.ExceptionData2

interface ManagedCallSitePayload{
    var producer: CTX?
    var handler: HandlerType?
    val message: String
    var code: Enum<*>?
    var cause: Throwable?
    var description: String?
    fun valueFailure(parameterName: String, parameterTypeName: String):ManagedCallSitePayload
    fun toDataWithTrace(stackTrace: List<StackTraceElement>): ExceptionData2
    fun addDescription(message: String):ManagedCallSitePayload
    fun create(producer: CTX, message: String = ""):ExceptionPayload{
        return ExceptionPayload(producer,  message)
    }
}



class ManagedPayload(
    val contextName: String,
    override val message: String,
    val trace: List<StackTraceElement>,
):ManagedCallSitePayload{
    var targetObject: String? = null

    override var handler: HandlerType? = null
    override var code: Enum<*>? = null
    override var cause: Throwable? = null
    override var description: String? = null

    override var producer: CTX? = null

    constructor(
        callingContext: CTX,
        message: String,
        trace: List<StackTraceElement>
    ): this(contextName = callingContext.contextName, message = message, trace = trace){
        producer = callingContext
    }

    override fun valueFailure(parameterName: String, parameterTypeName: String):ManagedCallSitePayload{
        return this
    }

    override fun addDescription(message: String):ManagedCallSitePayload{
        description = message
        return this
    }

    override fun create(producer: CTX, message: String):ExceptionPayload{
        return ExceptionPayload(producer,  message)
    }

    override fun toDataWithTrace(stackTrace: List<StackTraceElement>): ExceptionData2{
        return ExceptionData2(
            ManagedException.ExceptionEvent.Thrown,
            message,
            producer
        ).addStackTrace(stackTrace)
    }

    internal fun toData(auxData:  PrintableBase<*>? = null): ExceptionData2{
        return ExceptionData2(
            ManagedException.ExceptionEvent.Thrown,
            message,
            producer,
        ).provideAuxData(auxData)
    }

    override fun toString(): String {
        return message.textIfNull("")
    }
}


class ExceptionPayload(
    override var producer: CTX?,
    override var message: String = "",
    override var handler: HandlerType? = null,
    override var code: Enum<*>? = null,
    override var cause: Throwable? = null
):ManagedCallSitePayload{
    var targetObject: String? = null




    override var description: String? = null

    fun message(msg: String):ExceptionPayload{
        message = msg
        return this
    }

    override fun valueFailure(parameterName: String, parameterTypeName: String):ExceptionPayload{
        message = "$parameterName : $parameterTypeName"
        return this
    }

    override fun addDescription(message: String):ExceptionPayload{
        description = message
        return this
    }

    fun messageAndCode(msg: String, code: Enum<*>):ExceptionPayload{
       return ExceptionPayload(producer,  msg, handler, code)
    }

    fun method(methodName: String, expectedResult: String):ExceptionPayload{
        message =  "$methodName:$expectedResult"
        return this
    }

    fun provideDescription(methodName: String, reason: String, result: String){
        description +=  "In method: $methodName. Reason:${reason}. Result:${result}"
    }

    fun provideCode(exceptionCode: Enum<*>):ExceptionPayload{
        code = exceptionCode
        return this
    }

    fun provideCause(exception: Throwable):ExceptionData2{
        cause = exception
        message = exception.throwableToText()
        return ExceptionData2(
            ManagedException.ExceptionEvent.Thrown,
            message,
            producer
        ).addStackTrace(exception.stackTrace.toList())
    }

    override fun toDataWithTrace(stackTrace: List<StackTraceElement>): ExceptionData2{
        return ExceptionData2(
            ManagedException.ExceptionEvent.Thrown,
            message,
            producer
        ).addStackTrace(stackTrace)
    }

    internal fun toData(auxData:  PrintableBase<*>? = null): ExceptionData2{
        return ExceptionData2(
            ManagedException.ExceptionEvent.Thrown,
            message,
            producer,
        ).provideAuxData(auxData)
    }

    override fun toString(): String {
        return message.textIfNull("")
    }
}


fun CTX.toPayload(block: ExceptionPayload.()-> Unit):ExceptionPayload{
    val payload = ExceptionPayload(this)
    payload.block()
    return payload
}

fun CTX.toPayload(message: String):ExceptionPayload{
    val payload = ExceptionPayload(this, message)
    return payload
}

fun CTX.toPayload(cause: Throwable):ExceptionPayload{
    val payload = ExceptionPayload(this, cause = cause)
    return payload
}

