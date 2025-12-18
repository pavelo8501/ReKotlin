package po.misc.data.output

import po.misc.debugging.stack_tracer.Methods
import po.misc.exceptions.Tracer

object OutputDispatcher {

    var identifyOutput: Boolean = false

    fun locateOutputs(){
        val trace =   Tracer().createTrace(Methods("output"))
        val report =  trace.methodLocations("output")
        println(report)
    }
}

internal fun checkDispatcher(){
    if(OutputDispatcher.identifyOutput){
        OutputDispatcher.locateOutputs()
    }
}
