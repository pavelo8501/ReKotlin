package po.misc.exceptions.handling

import po.misc.exceptions.stack_trace.ExceptionTrace
import kotlin.reflect.KClass



//fun Throwable.traceFor(contextClass: KClass<*>):  ExceptionTrace{
//    return  createTrace(contextClass)
//}
//
//fun Throwable.traceFor(contextClass: KClass<*>, block: Throwable.(ExceptionTrace)-> Unit){
//   val trace = createTrace(contextClass)
//   block(trace)
//}
