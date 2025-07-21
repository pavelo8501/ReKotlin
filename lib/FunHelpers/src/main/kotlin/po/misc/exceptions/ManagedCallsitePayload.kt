package po.misc.exceptions

import po.misc.data.helpers.textIfNull
import po.misc.context.CTX
import po.misc.data.printable.PrintableBase
import po.misc.exceptions.models.ExceptionData2
//
//class ManagedCallSitePayload(
//    val producer: CTX,
//    var message: String = "",
//    val handler: HandlerType? = null,
//    val source: Enum<*>? = null,
//    val cause: Throwable? = null
//){
//    var targetObject: String? = null
//    var description: String? = null
//
//    fun message(msg: String):ManagedCallSitePayload{
//        message = msg
//        return this
//    }
//
//    fun valueFailure(parameterName: String, parameterTypeName: String):ManagedCallSitePayload{
//        message = "$parameterName : $parameterTypeName"
//        return this
//    }
//
//
//    fun method(methodName: String, expectedResult: String):ManagedCallSitePayload{
//        message =  "$methodName:$expectedResult"
//        return this
//    }
//
//    fun provideDescription(methodName: String, reason: String, result: String){
//        description +=  "In method: $methodName. Reason:${reason}. Result:${result}"
//    }
//
//    internal fun toDataWithTrace(stackTrace: List<StackTraceElement>): ExceptionData2{
//       return ExceptionData2(
//            ManagedException.ExceptionEvent.Thrown,
//            producer,
//            stackTrace = stackTrace
//        )
//    }
//
//    internal fun toData(auxData:  PrintableBase<*>? = null): ExceptionData2{
//        return ExceptionData2(
//            ManagedException.ExceptionEvent.Thrown,
//            producer,
//            auxData =  auxData
//        )
//    }
//
//    override fun toString(): String {
//        return message.textIfNull("")
//    }
//
//    companion object{
//
//        fun  create(producer: CTX, message: String = ""):ManagedCallSitePayload{
//           return ManagedCallSitePayload(producer,  message)
//        }
//    }
//}


interface ManagedCallSitePayload{
    val producer: CTX
    var handler: HandlerType?
    var message: String
    var source: Enum<*>?
    var cause: Throwable?
    var description: String?
    fun valueFailure(parameterName: String, parameterTypeName: String):ExceptionPayload
    fun toDataWithTrace(stackTrace: List<StackTraceElement>): ExceptionData2
    fun addDescription(message: String):ExceptionPayload
    fun create(producer: CTX, message: String = ""):ExceptionPayload{
        return ExceptionPayload(producer,  message)
    }
}



class ExceptionPayload(
    override val producer: CTX,
    override var message: String = "",
    override var handler: HandlerType? = null,
    override var source: Enum<*>? = null,
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

    fun method(methodName: String, expectedResult: String):ExceptionPayload{
        message =  "$methodName:$expectedResult"
        return this
    }

    fun provideDescription(methodName: String, reason: String, result: String){
        description +=  "In method: $methodName. Reason:${reason}. Result:${result}"
    }

    fun provideCode(code: Enum<*>):ExceptionPayload{
        source = code
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

