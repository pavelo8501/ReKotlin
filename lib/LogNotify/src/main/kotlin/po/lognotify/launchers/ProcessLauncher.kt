package po.lognotify.launchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import po.lognotify.TasksManaged
import po.lognotify.process.LoggerProcess
import po.lognotify.process.Process
import po.lognotify.process.createProcess
import po.misc.coroutines.LauncherType
import po.misc.interfaces.Processable

@PublishedApi
internal suspend fun <T, R> executeProcess(
    process: Process<T>,
    block: suspend LoggerProcess<T>.(Process<T>)-> R,
):R where T: Processable = LauncherType.ConcurrentLauncher.RunCoroutineHolder(process) {
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
        val result = process.block(process)
        process.complete(result)
    }

suspend inline fun <reified T, R> runProcess(
    receiver: T,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    noinline block: suspend LoggerProcess<T>.(Process<T>)-> R,
):R where T: Processable{

    val process = createProcess(receiver, dispatcher)
    TasksManaged.LogNotify.taskDispatcher.registerProcess(process)
    return executeProcess(process, block)
}


inline fun <reified T, R> runProcessBlocking(
    receiver: T,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    noinline block: suspend LoggerProcess<T>.(Process<T>)-> R,
):R where T: Processable{
    val process = createProcess(receiver, dispatcher)
    TasksManaged.LogNotify.taskDispatcher.registerProcess(process)
    return runBlocking {
        executeProcess(process, block)
    }
}


