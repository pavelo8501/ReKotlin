package po.lognotify.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import po.lognotify.classes.process.LoggProcess
import po.lognotify.classes.process.ProcessableContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext


internal fun   CoroutineContext.currentProcess():LoggProcess<*>?{
   return this[LoggProcess]
}


suspend inline fun<T: ProcessableContext<T>, R>  T.launchProcess(crossinline block: suspend CoroutineScope.()-> R):R{
    try {
        val logProcess = LoggProcess(this, coroutineContext)
        return CoroutineScope(logProcess + this).async{
            logProcess.startRun(this.coroutineContext)
             val result =  block.invoke(this)
            logProcess.stopRun()
            result
        }.await()
    }catch (th: Throwable){
        println(th.message)
        throw th
    }
}