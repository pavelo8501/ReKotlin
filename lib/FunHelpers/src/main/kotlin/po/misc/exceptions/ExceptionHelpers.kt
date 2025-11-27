package po.misc.exceptions

import po.misc.context.tracable.TraceableContext
import po.misc.data.helpers.orDefault
import po.misc.data.helpers.stripAfter
import po.misc.debugging.stack_tracer.StackFrameMeta


fun throwManaged(
    message: String,
    callingContext: Any
): Nothing{
    val methodName = "throwManaged"
    val payload =  ManagedPayload(message, methodName, callingContext)
    throw ManagedException(payload)
}

fun <S: Enum<S>> throwException(
    message: String,
    handler : HandlerType,
    callingContext: Any,
    exceptionProvider: (ThrowableCallSitePayload)-> Throwable
): Nothing{
    val methodName = "throwException"
    val payload =  ManagedPayload(message, methodName, callingContext)
    throw exceptionProvider.invoke(payload.setHandler(handler))
}

fun <S: Enum<S>> throwException(
    message: String,
    callingContext: Any,
    code: S? = null,
    handler : HandlerType? = null,
    exceptionProvider: (ThrowableCallSitePayload)-> Throwable
): Nothing {
    val methodName = "throwException"
    val payload =  ManagedPayload(message, methodName, callingContext)
    val completePayload = payload.setCode(code).setHandler(handler)
    throw exceptionProvider.invoke(completePayload)
}

fun Throwable.toManaged(context: TraceableContext): ManagedException{
    val managed = ManagedException(context, message = throwableToText(), code = null, cause = this)
    return managed
}

fun  Throwable.throwableToText(): String{
    return buildString {
        appendLine(this@throwableToText.javaClass.simpleName)
        appendLine("Message: " +  message.orDefault("-") )
    }
}


fun StackFrameMeta.toStackTraceFormat(): String {
    val simpleClassName = fileName.substringAfterLast('.')
    val fileName = "$simpleClassName.kt" // or use `.java` if applicable
    return "\tat ${fileName.stripAfter('$')}.$methodName($fileName:$lineNumber)"
}


