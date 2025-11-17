package po.misc.exceptions.stack_trace

import po.misc.collections.takeFromMatch
import po.misc.context.tracable.TraceableContext
import po.misc.data.helpers.output
import po.misc.debugging.ClassResolver
import po.misc.debugging.models.ClassInfo
import po.misc.exceptions.ContextTracer
import po.misc.exceptions.classifier.PackageRole
import po.misc.exceptions.ThrowableCallSitePayload
import po.misc.exceptions.classifier.classifyPackage
import po.misc.exceptions.models.CTXResolutionFlag
import po.misc.exceptions.stack_trace.extractTrace
import po.misc.exceptions.throwableToText
import po.misc.exceptions.trackable.TrackableException
import po.misc.types.helpers.simpleOrAnon
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.text.substringAfterLast


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

fun Throwable.extractTrace(analyzeDepth: Int = 10): ExceptionTrace {
    val depth = analyzeDepth.coerceAtLeast(10)
    val frames =  stackTrace.take(depth).toMeta()
    val filtered = frames.filter {frameMeta ->
        ExceptionTrace.harshFilter(frameMeta)
    }
    val trace = if(filtered.isNotEmpty()){
        ExceptionTrace(throwableToText(), filtered, reliable = true)
    }else{
        ExceptionTrace(throwableToText(), frames, reliable = false)
    }
    return trace
}


