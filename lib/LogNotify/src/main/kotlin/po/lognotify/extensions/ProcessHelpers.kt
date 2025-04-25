package po.lognotify.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import po.lognotify.classes.process.LoggProcess
import po.lognotify.classes.process.ProcessableContext
import kotlin.coroutines.CoroutineContext


internal val CoroutineContext.process: LoggProcess<*>?
    get() = this[LoggProcess]


suspend inline  fun<T: ProcessableContext<*>, R>  T.launchProcess(crossinline block: suspend CoroutineScope.()-> R):R{
    try {
        val receiver = this.castOrLoggerException<ProcessableContext<T>>()
        return CoroutineScope(LoggProcess(receiver)  +  receiver).async{
            block.invoke(this)
        }.await()
    }catch (th: Throwable){
        println(th.message)
        throw th
    }
}