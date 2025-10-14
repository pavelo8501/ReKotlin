package po.misc.exceptions

import po.misc.collections.takeFromMatch
import po.misc.context.TraceableContext
import po.misc.data.helpers.replaceIfNull
import po.misc.data.helpers.stripAfter
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.exceptions.stack_trace.StackFrameMeta

internal fun  Throwable.currentCallerTrace(methodName: String): List<StackTraceElement> {
    return stackTrace.takeFromMatch(2){ it.methodName == methodName }
}


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
): Nothing{
    val methodName = "throwException"
    val payload =  ManagedPayload(message, methodName, callingContext)
    val completePayload = payload.setCode(code).setHandler(handler)
    throw exceptionProvider.invoke(completePayload)
}


fun Throwable.toManaged(context: TraceableContext): ManagedException{

    val managed = ManagedException(context, message = throwableToText(), code = null, cause = this)
    return managed
}

fun Throwable.toInfoString(): String{
    val base = this.javaClass.simpleName
    val msg = message ?: ""
    val cause = cause?.let { " | Cause: ${it.javaClass.simpleName} - ${it.message ?: "No message"}" } ?: ""
    return "$base: $msg$cause"
}

fun  Throwable.throwableToText(): String{

    return buildString {
        appendLine(this@throwableToText.javaClass.simpleName)
        appendLine("Message: " +  message.replaceIfNull("-") )
    }
}


internal fun String.isLikelyUserCode(): Boolean {
    return this.isNotBlank() &&
            !startsWith("kotlin") &&
            !startsWith("java") &&
            !startsWith("sun") &&
            !startsWith("jdk") &&
            !startsWith("org.jetbrains")
}

fun Throwable.extractCallSiteMeta(
    methodName: String,
    framesCount: Int = 2,
    helperPackagePrefixes: List<String> = listOf("po.misc", "kotlin", "java")
): List<StackFrameMeta> {

    val frames = stackTrace.takeFromMatch<StackTraceElement>(framesCount){ it.methodName  ==  methodName}

   return frames.map {stackTraceElement->
        val classPackage = stackTraceElement.className.substringBeforeLast('.', missingDelimiterValue = "")
        val isHelper = helperPackagePrefixes.any { prefix -> stackTraceElement.className.startsWith(prefix) }
        val isUser = !isHelper && classPackage.isLikelyUserCode()
        StackFrameMeta(
            fileName = stackTraceElement.className,
            simpleClassName = stackTraceElement.javaClass.simpleName,
            methodName = stackTraceElement.methodName,
            lineNumber = stackTraceElement.lineNumber,
            classPackage = classPackage,
            isHelperMethod = isHelper,
            isUserCode = isUser
        )
    }
}


fun toStackTraceFormat(fileName: String, lineNumber: Int): String{
    return "\tat ($fileName:$lineNumber)"
}

fun classNameToFile(className: String): String{
    val simpleClassName = className.substringAfterLast('.')
   return "$simpleClassName.kt"
}

fun StackFrameMeta.toStackTraceFormat(): String {
    val simpleClassName = fileName.substringAfterLast('.')
    val fileName = "$simpleClassName.kt" // or use `.java` if applicable
    return "\tat ${fileName.stripAfter('$')}.$methodName($fileName:$lineNumber)"
}


