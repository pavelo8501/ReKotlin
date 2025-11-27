package po.misc.debugging.stack_tracer

import po.misc.context.tracable.TraceableContext
import po.misc.debugging.classifier.PackageClassifier
import po.misc.exceptions.TraceCallSite
import po.misc.exceptions.Tracer
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.stack_trace.extractTrace
import po.misc.types.k_class.simpleOrAnon
import po.misc.types.k_function.receiverClass
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

interface StackTracer {

    fun KFunction<*>.createTrace(classifier: PackageClassifier? = null):  ExceptionTrace{

        return  traceCallSite(this, classifier)
    }
    fun  KClass<out TraceableContext>.createTrace(classifier: PackageClassifier? = null):  ExceptionTrace{
        return  traceCallSite(this, classifier)
    }

    companion object : StackTracerClass()

}

open class StackTracerClass {

    fun traceCallSite(function: KFunction<*>, classifier: PackageClassifier? = null):  ExceptionTrace{
        val receiver = function.receiverClass()
        val className =  receiver?.simpleOrAnon?:"N/A"
        val traceOption = TraceCallSite(className, null)
        return Tracer().extractTrace(traceOption, classifier = classifier)
    }

    fun traceCallSite(kClass: KClass<out TraceableContext>, classifier: PackageClassifier? = null):  ExceptionTrace{
        val traceOption = TraceCallSite(kClass.simpleOrAnon, null)
        traceOption.className = kClass.simpleOrAnon
        return Tracer().extractTrace(traceOption, classifier = classifier)
    }

}