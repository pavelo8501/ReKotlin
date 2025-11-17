package po.misc.exceptions.stack_trace

import po.misc.collections.takeFromMatch
import po.misc.context.tracable.TraceableContext
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


fun Throwable.extractTrace(): ExceptionTrace {
    val meta =  stackTrace.take(5).toMeta()
    return  meta.firstOrNull { it.isUserCode }?.let {
        ExceptionTrace(this,  meta).setBestPick(it)
    }?:run {
        ExceptionTrace(this,  meta)
    }
}

fun Throwable.extractTrace(traceable: TraceableContext): ExceptionTrace {

    val takeForAnalysis = 30
    val contextClass = traceable::class
    val frames =  stackTrace.take(takeForAnalysis).toMeta()
    val convertedToMeta = frames.takeFromMatch(5) {
            it.simpleClassName.equals(contextClass.simpleOrAnon, ignoreCase = true)
        }
    return if(convertedToMeta.isEmpty()){
        "extractTrace selected first $takeForAnalysis frames for analysis, but was unable to find any records for $contextClass" +
        "Using first $takeForAnalysis as fallback. Exception location is unreliable"
        val trace = ExceptionTrace(this.throwableToText(), frames, contextClass)
        trace.reliable = false
        trace
    }else{
        ExceptionTrace(this.throwableToText(), convertedToMeta, contextClass)
    }
}

fun Throwable.tryExtractTrace(context: Any, cause: Throwable? = null): ExceptionTrace{

   return when(context){
        is TraceableContext -> {

            cause?.extractTrace(traceable = context) ?:run {
                extractTrace(traceable = context)
            }
        }
        else -> extractTrace()
    }
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


fun TraceableContext.extractTrace(withClassInfo: Boolean):  ClassInfo {
    val trace =  ContextTracer(this, CTXResolutionFlag.Resolvable).exceptionTrace
    val info = ClassResolver.classInfo(this)
    return info.addTraceInfo(trace.bestPick)

}

fun TraceableContext.extractTrace():  ExceptionTrace {
   return ContextTracer(this, CTXResolutionFlag.Resolvable).exceptionTrace
}


