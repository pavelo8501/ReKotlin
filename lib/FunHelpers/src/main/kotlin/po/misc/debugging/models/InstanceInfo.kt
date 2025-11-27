package po.misc.debugging.models

import po.misc.data.PrettyPrint
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.debugging.stack_tracer.StackFrameMeta
import po.misc.time.TimeHelper


class InstanceInfo(
    name: String,
    val instanceHash: Int,
    val classInfo: ClassInfo,
): PrettyPrint, TimeHelper {


    val className: String get() = classInfo.simpleName
    val instanceName: String = "$name # $instanceHash"

    internal  val traces = mutableListOf<ExceptionTrace>()
    override val formattedString: String get() = buildString {
       if(traces.isEmpty()){
           append("${classInfo.formattedString} # $instanceHash")
       }else{
           appendLine("${classInfo.formattedString} # $instanceHash")
            val traceStr =  traces.joinToString {
               val time = it.created.toLocalTime()
               appendLine("Stack Trace @ $time")
               it.bestPick.formattedString
           }
           append(traceStr)
       }
    }

    val latestFrameMeta: StackFrameMeta? get() =  traces.lastOrNull()?.bestPick

    fun addTraceInfo(trace : ExceptionTrace): InstanceInfo{
        traces.add(trace)
        return this
    }

    override fun toString(): String = instanceName

}

