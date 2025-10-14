package po.misc.exceptions.stack_trace

import po.misc.collections.takeFromMatch
import po.misc.context.TraceableContext
import po.misc.exceptions.PackageRole
import po.misc.exceptions.ThrowableCallSitePayload
import po.misc.exceptions.classifyPackage
import po.misc.exceptions.throwableToText
import po.misc.types.helpers.simpleOrAnon
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.text.substringAfterLast



private fun analyzeException(){

}

fun StackTraceElement.toMeta(): StackFrameMeta {
    val classPackage = className.substringBeforeLast('.', missingDelimiterValue = "")
    val role = classifyPackage(classPackage)
    return StackFrameMeta(
        fileName = this.fileName?:"N/A",
        simpleClassName = className.substringAfterLast('.'),
        methodName = methodName,
        lineNumber = lineNumber,
        classPackage = classPackage,
        isHelperMethod = role == PackageRole.Helper,
        isUserCode = role == PackageRole.User,
        stackTraceElement = this
    )
}

fun List<StackTraceElement>.toMeta(): List<StackFrameMeta> = map { it.toMeta() }


fun Throwable.extractTrace(): ExceptionTrace {
    val meta =  stackTrace.take(5).toMeta()
    return  meta.firstOrNull { it.isUserCode }?.let {
        ExceptionTrace(this,  meta).setBestPick(it)
    }?:run {
        ExceptionTrace(this,  meta)
    }
}

fun Throwable.extractTrace(context: TraceableContext): ExceptionTrace {

    val contextClass = context::class
    val frames =  stackTrace.take(20).toMeta()

    val convertedToMeta = frames.takeFromMatch(5) {
            it.simpleClassName.equals(contextClass.simpleOrAnon, ignoreCase = true)
        }
    return  ExceptionTrace(this.throwableToText(), convertedToMeta, contextClass)
}


fun Throwable.extractTrace(
    exceptionPayload: ThrowableCallSitePayload
): ExceptionTrace {

    fun analyze(
        elements: List<StackTraceElement>,
        className: String,
        helperMethodName: String?,
    ): List<StackFrameMeta> {

        val convertedToMeta = elements.take(5).toMeta()
        val searchResult = if (helperMethodName != null) {
            //If its a helper than we will star searching from methods name
             val selected = convertedToMeta.takeFromMatch(5) {
                it.methodName == helperMethodName
            }
            selected.drop(1)
        } else {
            //If it is not helpers method name or javaClassName is a real context name search until class name found
            // and select firs method in the list
            convertedToMeta.takeFromMatch(5) {
                it.simpleClassName.equals(className, ignoreCase = true)
            }
        }
        return searchResult.ifEmpty {
            convertedToMeta
        }
    }
    val contextClass = exceptionPayload.context::class
    val meta =  if(exceptionPayload.helperMethodName){
        analyze(stackTrace.toList(), contextClass.simpleOrAnon,exceptionPayload.methodName)
    }else{
        analyze(stackTrace.toList(), contextClass.simpleOrAnon, null)
    }
    return if(contextClass.isSubclassOf(TraceableContext::class)){
        @Suppress("UNCHECKED_CAST")
        ExceptionTrace(this,  meta, contextClass  as KClass<out TraceableContext>)
    }else{
        ExceptionTrace(this,  meta)
    }
}
