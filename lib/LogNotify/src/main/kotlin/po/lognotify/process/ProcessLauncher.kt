package po.lognotify.process

import kotlinx.coroutines.CoroutineDispatcher
import po.misc.coroutines.CoroutineHolder
import po.misc.coroutines.LauncherType


suspend fun <T, R> T.runProcess(
    processName: String,
    dispatcher: CoroutineDispatcher,
    block: suspend T.()-> R,
):R where T: CoroutineHolder {

    val receiver = this
    val ridingObject: String = this::class.simpleName.toString()
    println("runProcess ridingObject $ridingObject")
   return LauncherType.ConcurrentLauncher.RunCoroutineHolder(receiver, dispatcher) {
       val process = LoggerProcess(processName, receiver, block)
        process.launchProcess()
    }
}
