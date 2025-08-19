package po.lognotify.process

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import po.lognotify.TasksManaged
import po.misc.interfaces.Processable
import po.misc.types.safeCast
import kotlin.coroutines.CoroutineContext

fun  CoroutineContext.loggerProcess(): Process<*>? = this[Process]

inline fun <reified T: Processable> CoroutineContext.process(): Process<T>? {
   return this[Process]?.safeCast<Process<T>>()
}

inline  fun <reified T: Processable> processInContext(context: CoroutineContext): Process<T>? {
    return context[Process]?.safeCast<Process<T>>()
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

inline fun <reified T: Processable> TasksManaged.activeProcess(): Process<T>?{
    return logHandler.dispatcher.activeProcess()?.safeCast<Process<T>>()
}


