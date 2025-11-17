package po.misc.debugging.models

import po.misc.data.PrettyPrint
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.stack_trace.StackFrameMeta
import po.misc.time.TimeHelper


class InstanceInfo(
    val name: String,
    val instanceHash: Int,
    val classInfo: ClassInfo,
): PrettyPrint, TimeHelper {


    internal  val traces = mutableListOf<ExceptionTrace>()

    override val formattedString: String
        get() {
           return buildString {
                appendLine("${classInfo.formattedString} # $instanceHash")
                if(traces.isNotEmpty()){
                    val traceStr =  traces.joinToString {
                        val time = it.created.toLocalTime()
                        appendLine("Stack Trace @ $time")
                        it.bestPick.formattedString
                    }
                   appendLine(traceStr)
                }
            }
        }

    val latestFrameMeta: StackFrameMeta? get() =  traces.lastOrNull()?.bestPick

    fun addTraceInfo(trace : ExceptionTrace): InstanceInfo{
        traces.add(trace)
        return this
    }

    override fun toString(): String = name

}

