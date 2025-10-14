package po.misc.exceptions.models

import po.misc.exceptions.ManagedException.ExceptionEvent
import po.misc.context.CTX
import po.misc.data.printable.knowntypes.PropertyData
import po.misc.exceptions.stack_trace.StackFrameMeta

@Deprecated("ExceptionTrace already contains all information needed")
class ExceptionData(
    val event: ExceptionEvent,
    val message: String,
    val producer: CTX?,
){

    var thisStackTraceElement: StackTraceElement? = null
        private set

    var stackTraceList: List<StackTraceElement> = emptyList()
        private set

    var propertySnapshotBacking:  List<PropertyData> = listOf()
    val propertySnapshot : List<PropertyData> get() = propertySnapshotBacking

    private var stackTraceBacking: List<StackFrameMeta> = listOf()
    val stackTrace :  List<StackFrameMeta> get() = stackTraceBacking

    fun addPropertySnapshot(snapshot:  List<PropertyData>){
        propertySnapshotBacking = snapshot
    }

    fun addStackTrace(stackTrace: List<StackTraceElement>):ExceptionData{
        stackTraceList = stackTrace
        return this
    }

    fun addStackTraceMeta(stackFrames: List<StackFrameMeta>):ExceptionData{
        stackTraceBacking = stackFrames
        return this
    }
}
