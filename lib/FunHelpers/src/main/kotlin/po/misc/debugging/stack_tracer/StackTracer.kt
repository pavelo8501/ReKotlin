package po.misc.debugging.stack_tracer

import po.misc.context.tracable.TraceableContext
import po.misc.debugging.classifier.PackageClassifier
import po.misc.exceptions.TraceCallSite
import po.misc.exceptions.Tracer
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.stack_trace.extractTrace
import po.misc.types.k_class.simpleOrAnon
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

interface StackTracer {

    fun KFunction<*>.createTrace(classifier: PackageClassifier? = null):  ExceptionTrace{
        return traceCallSite(this, classifier)
    }

    fun TraceableContext.createTrace(methodName: String? = null,  classifier: PackageClassifier? = null):  ExceptionTrace{
        return  traceCallSite(this::class, methodName, classifier)
    }
    
    fun  KClass<out TraceableContext>.createTrace(classifier: PackageClassifier? = null):  ExceptionTrace{
        return  traceCallSite(this, null,  classifier)
    }

    companion object : StackTracerClass()
}

open class StackTracerClass {

    fun traceCallSite(className: String, methodName: String?, classifier: PackageClassifier? = null):  ExceptionTrace{
        val traceOption = TraceCallSite(className, methodName)
        return Tracer().extractTrace(traceOption, classifier = classifier)
    }

    fun traceCallSite(function: KFunction<*>, classifier: PackageClassifier? = null):  ExceptionTrace{
        val traceOption =  TraceCallSite(function)
        return Tracer().extractTrace(traceOption, classifier = classifier)
    }

    fun traceCallSite(kClass: KClass<*>, methodName: String?,  classifier: PackageClassifier? = null):  ExceptionTrace{
        val traceOption = TraceCallSite(kClass.simpleOrAnon, methodName)
        return Tracer().extractTrace(traceOption, classifier = classifier)
    }
}