package po.lognotify.process

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import po.lognotify.TasksManaged
import po.misc.data.helpers.output
import po.misc.interfaces.Processable
import kotlin.coroutines.CoroutineContext


fun  CoroutineContext.loggerProcess(): Process<*>? = this[Process]

fun processInContext(context: CoroutineContext): Process<*>? {
   return context[Process]
}

inline fun <reified T> createProcess(
    receiver:T,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
):Process<T> where T:Processable{

   val key = ProcessKey.create<T>(CoroutineName(receiver.identity.identifiedByName))
   return Process(key,receiver, dispatcher)
}

fun <T> TasksManaged.processLookUp(
    processKey: ProcessKey<T>
): Process<T>? where T: Processable{
   return logHandler.dispatcher.lookUpProcess(processKey)
}

fun  TasksManaged.activeProcess(): Process<*>?{
    logHandler.dispatcher.hashCode().output()
    return logHandler.dispatcher.activeProcess()
}



