package po.misc.exceptions

import po.misc.collections.takeFromMatch
import po.misc.data.helpers.stripAfter
import po.misc.exceptions.models.StackFrameMeta


internal fun  Throwable.currentCallerTrace(methodName: String): List<StackTraceElement> {
    return stackTrace.takeFromMatch(2){ it.methodName == methodName }
}

fun throwManaged(
    message: String,
    callingContext: Any,
    code: Enum<*>? = null,
    handler : HandlerType? = null,
): Nothing{
    val methodName = "throwManaged"
    val payload =  ManagedPayload(message, methodName, callingContext)
    throw ManagedException(payload.setCode(code).setHandler(handler))
}

fun throwManaged(
    message: String,
    handler : HandlerType,
    callingContext: Any
): Nothing{
    val methodName = "throwManaged"
    val payload =  ManagedPayload(message, methodName, callingContext)
    throw ManagedException(payload.setHandler(handler))
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
    exceptionProvider: (ManagedCallSitePayload)-> Throwable
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
    exceptionProvider: (ManagedCallSitePayload)-> Throwable
): Nothing{
    val methodName = "throwException"
    val payload =  ManagedPayload(message, methodName, callingContext)
    val completePayload = payload.setCode(code).setHandler(handler)
    throw exceptionProvider.invoke(completePayload)
}


inline fun <S: Enum<S>> throwException(
    message: String,
    callingContext: Any,
    exceptionProvider: (ManagedCallSitePayload)-> Throwable
): Nothing{
    val methodName = "throwManageable"
    val payload = ManagedPayload(message, methodName, callingContext)
    throw exceptionProvider.invoke(payload)
}

fun Throwable.toManaged(): ManagedException{
    val managed = ManagedException(this.throwableToText(), null, this)
    return managed
}

fun Throwable.toInfoString(): String{
    val base = this.javaClass.simpleName
    val msg = message ?: ""
    val cause = cause?.let { " | Cause: ${it.javaClass.simpleName} - ${it.message ?: "No message"}" } ?: ""
    return "$base: $msg$cause"
}

fun  Throwable.throwableToText(): String{
   return if(this.message != null){
        this.message.toString()
    }else{
        this.javaClass.simpleName.toString()
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


