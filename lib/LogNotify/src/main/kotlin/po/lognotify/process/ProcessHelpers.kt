package po.lognotify.process

import po.misc.context.CTX
import kotlin.coroutines.CoroutineContext


fun  CoroutineContext.processInScope(): Process<*>? = this[Process]

fun CTX.processInScope(context: CoroutineContext): Process<*>? {
   return context[Process]
}