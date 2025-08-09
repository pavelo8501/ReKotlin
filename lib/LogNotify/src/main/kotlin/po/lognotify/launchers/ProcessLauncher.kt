package po.lognotify.launchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import po.lognotify.exceptions.getOrLoggerException
import po.lognotify.process.LoggerProcess
import po.lognotify.process.Process
import po.misc.context.CTX
import po.misc.coroutines.CoroutineHolder
import po.misc.coroutines.LauncherType
import po.misc.data.logging.LogCollector
import kotlin.coroutines.CoroutineContext


private suspend fun <T, R> executeProcess(
    process: Process<T>,
    dispatcher: CoroutineDispatcher,
    block: suspend LoggerProcess<T>.()-> R,
):R where T: CoroutineHolder, T: CTX, T: LogCollector{

    return LauncherType.ConcurrentLauncher.RunCoroutineHolder(process, dispatcher) {
        val job = process.coroutineContext[Job]
        job?.invokeOnCompletion { cause ->
            if (cause == null) {
                println("Coroutine completed normally")
            } else {
                println("Coroutine was cancelled or failed: ${cause.message}")
            }
        }?:run {
            println("No Job found")
        }
        process.block()
    }
}

@JvmName("runProcessAttached")
suspend fun <T, R> T.runProcess(
    contextKey:CoroutineContext.Key<*>,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    block: suspend LoggerProcess<T>.()-> R,
):R where T: CoroutineHolder, T: CTX, T: LogCollector{

   val element =  this.coroutineContext[contextKey]
   val notNullElement = element.getOrLoggerException("CoroutineElement is null for key provided : $contextKey")
   val process = Process<T>(identifiedByName,this, notNullElement)
   return executeProcess(process, dispatcher, block)
}

suspend fun <T, R> runProcess(
    receiver: T,
    contextKey:CoroutineContext.Key<*>,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    block: suspend LoggerProcess<T>.()-> R,
):R where T: CoroutineHolder, T: CTX, T: LogCollector{

    val element = receiver.coroutineContext[contextKey]
    val notNullElement = element.getOrLoggerException("CoroutineElement is null for key provided : $contextKey")
    val process = Process<T>(receiver.identifiedByName, receiver, notNullElement)
    return executeProcess(process, dispatcher, block)

}
