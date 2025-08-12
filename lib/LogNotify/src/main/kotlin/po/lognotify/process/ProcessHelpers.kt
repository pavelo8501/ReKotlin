package po.lognotify.process

import kotlinx.coroutines.CoroutineName
import po.lognotify.TasksManaged
import po.misc.context.CTX
import po.misc.data.logging.LogCollector
import kotlin.coroutines.CoroutineContext


fun  CoroutineContext.processInContext(): Process<*>? = this[Process]

fun CTX.processInContext(context: CoroutineContext): Process<*>? {
   return context[Process]
}

inline fun <reified T> createProcess(
    receiver:T
):Process<T> where T: CTX, T: LogCollector, T: CoroutineContext.Element{
   val key = ProcessKey.create<T>(CoroutineName(receiver.identity.identifiedByName))
   return Process(key, receiver)
}


fun <T> TasksManaged.processLookUp(processKey: ProcessKey<T>): Process<T>? where T: CTX, T: LogCollector, T: CoroutineContext.Element{
   return logHandler.dispatcher.lookUpProcess(processKey)
}

fun  TasksManaged.activeProcess(): Process<*>?{
    return logHandler.dispatcher.activeProcess()
}



