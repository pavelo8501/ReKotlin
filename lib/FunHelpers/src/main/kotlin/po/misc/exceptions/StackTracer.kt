package po.misc.exceptions


import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.stack_trace.extractTrace
import kotlin.reflect.KFunction

interface StackTracer {


    fun KFunction<*>.trace():  ExceptionTrace{
       return  Companion.traceCallSite(this)
    }
    companion object : StackTracerClass()
}

open class StackTracerClass {
    fun traceCallSite(function: KFunction<*>):  ExceptionTrace{
        return Tracer().extractTrace(TraceCallSite(function))
    }
}