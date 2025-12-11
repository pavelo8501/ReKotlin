package po.misc.debugging.stack_tracer

import po.misc.context.tracable.TraceableContext
import po.misc.debugging.classifier.HelperRecord
import po.misc.debugging.classifier.KnownHelpers
import po.misc.debugging.classifier.PackageClassifier
import po.misc.debugging.classifier.SimplePackageClassifier
import po.misc.exceptions.Tracer
import po.misc.debugging.stack_tracer.ExceptionTrace
import po.misc.types.k_class.simpleOrAnon
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

interface StackTracer {
    fun KFunction<*>.createTrace(classifier: PackageClassifier? = null):  ExceptionTrace{
        return traceCallSite(this.name, null,  classifier)
    }
    fun TraceableContext.createTrace(methodName: String,  classifier: PackageClassifier? = null):  ExceptionTrace{
        return  traceCallSite(methodName, this::class.simpleOrAnon, classifier)
    }
    fun  KClass<out TraceableContext>.createTrace(methodName: String,  classifier: PackageClassifier? = null):  ExceptionTrace{
        return  traceCallSite(methodName, this.simpleOrAnon,  classifier)
    }
    companion object : StackTracerClass()
}


open class StackTracerClass(
    val simpleClassifier: SimplePackageClassifier = SimplePackageClassifier(KnownHelpers)
){

    fun addHelperRecords(records: List<HelperRecord>):StackTracerClass{
        simpleClassifier.addHelperRecords(records)
        return this
    }
    fun addHelperRecords(vararg record: HelperRecord):StackTracerClass =
        addHelperRecords(record.toList())

    fun traceCallSite(
        methodName: String,
        className: String? = null,
        classifier: PackageClassifier? = null
    ):  ExceptionTrace{

        val useClassifier = classifier?:simpleClassifier
        val options = CallSite(methodName, className)
        val trace = Tracer(options, useClassifier)
        return trace.trace
    }
}