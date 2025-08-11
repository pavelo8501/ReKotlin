package po.lognotify.launchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import po.lognotify.TasksManaged
import po.lognotify.process.LoggerProcess
import po.lognotify.process.Process
import po.lognotify.process.ProcessKey
import po.misc.context.CTX
import po.misc.coroutines.LauncherType
import po.misc.data.logging.LogCollector
import java.util.UUID
import kotlin.coroutines.CoroutineContext

@PublishedApi
internal suspend fun <T, R> executeProcess(
    process: Process<T>,
    dispatcher: CoroutineDispatcher,
    block: suspend LoggerProcess<T>.()-> R,
):R where T: CTX, T: LogCollector, T: CoroutineContext.Element{

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
suspend inline fun <reified T, R> T.runProcess(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    noinline block: suspend LoggerProcess<T>.()-> R,
):R where T: CTX, T: LogCollector, T: CoroutineContext.Element{

   val processKey = ProcessKey.create<T>("Process#${UUID.randomUUID()}", CoroutineName(identity.identifiedByName))
   val process = Process<T>(processKey,this)
    TasksManaged.LogNotify.taskDispatcher.registerProcess(process)
   return executeProcess(process, dispatcher, block)
}

suspend inline fun <reified T, R> runProcess(
    receiver: T,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    noinline block: suspend LoggerProcess<T>.()-> R,
):R where T: CTX, T: LogCollector, T: CoroutineContext.Element{

    val processKey = ProcessKey.create<T>("Process#${UUID.randomUUID()}", CoroutineName(receiver.identity.identifiedByName))
    val process = Process<T>(processKey,  receiver)
    TasksManaged.LogNotify.taskDispatcher.registerProcess(process)
    return executeProcess(process, dispatcher, block)

}
