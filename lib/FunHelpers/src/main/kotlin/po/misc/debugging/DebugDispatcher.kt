package po.misc.debugging

import po.misc.context.tracable.TraceableContext


class DebugEvent<T: Any>(
    val tag: Enum<*>,
    val lambda: T.()-> DebugFrameData
)

class DebugDispatcher {

    val debugEvents = mutableListOf<DebugEvent<*>>()

    fun <T: TraceableContext> T.debugEvent(block:  (T)-> DebugFrameData){
        val event = DebugEvent(DebugTopic.General, block)
        debugEvents.add(event)
    }

    fun <T: TraceableContext> debugWith(context:T,  block:  T.()-> Unit){
        context.block()
    }
}