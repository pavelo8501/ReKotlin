package po.test.misc.exceptions.setup

import po.misc.context.tracable.TraceableContext
import po.misc.exceptions.stack_trace.ExceptionTrace


class TraceNotifier(var notifyOnValue: Int,  var name: String = "TraceNotifier"): TraceableContext {

    fun notifyOrNot(value: Int): ExceptionTrace?{
        if(notifyOnValue == value){
            val trace  = ::notifyOrNot.trace()
            return trace
        }else{
            return null
        }
    }
}

fun TraceNotifier.notifyOrNotExtension(value: Int): ExceptionTrace?{
    return notifyOrNot(value)
}