package po.misc.data.output

import po.misc.debugging.stack_tracer.TraceOptions
import po.misc.exceptions.Tracer

object OutputDispatcher {

    var identifyOutput: Boolean = false

    fun locateOutputs(){
        val trace =   Tracer().createTrace(TraceOptions.Method("output"))
        val report =  trace.methodLocations("output")
        println(report)
    }
}

internal fun checkDispatcher(){
    if(OutputDispatcher.identifyOutput){
        OutputDispatcher.locateOutputs()
    }
}
