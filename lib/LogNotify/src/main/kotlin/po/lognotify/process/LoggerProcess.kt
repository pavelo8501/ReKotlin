package po.lognotify.process

import po.misc.coroutines.CoroutineHolder
import po.misc.context.Identifiable
import po.misc.time.ExecutionTimeStamp


class LoggerProcess<T, R>(
    val processName: String,
    val ctx: T,
    val block: suspend T.()->R
) where T: CoroutineHolder {

   // val identified :  Identifiable = asIdentifiable(processName, "LoggerProcess")
    val executionTimeStamp: ExecutionTimeStamp = ExecutionTimeStamp(processName, "LoggerProcess")

    suspend fun launchProcess():R{
       return block.invoke(ctx)
    }

}


//
//class LoggProcess<E: ProcessableContext<*>>(
//    private val holder: ProcessableContext<E>,
//    private val context: CoroutineContext
//) : CoroutineContext.Element, MeasuredContext {
//
//    val identifiedAs : String get() = holder.identifiedAs
//    val name : String get() = holder.name
//
//    var coroutineInfo : CoroutineInfo = CoroutineInfo.createInfo(context)
//    private val listenerJobs = mutableMapOf<TaskKey, Job>()
//
//    override val executionTimeStamp: ExecutionTimeStamp = ExecutionTimeStamp(name, identifiedAs)
//
//    init {
//        initialize()
//    }
//
//    suspend fun getCoroutineInfo(): CoroutineInfo{
//        return CoroutineInfo.createInfo(coroutineContext)
//    }
//
//    suspend fun startRun(context: CoroutineContext){
//        coroutineInfo = CoroutineInfo.createInfo(context)
//    }
//
//    fun stopRun(): LoggProcess<E>{
//        return this
//    }
//
//    fun initialize(){
//        executionTimeStamp.onStart {
//
//        }
//
//        executionTimeStamp.onStop {
//
//        }
//        holder.getLoggerProcess = {
//            this
//        }
//    }
//    suspend fun observeTask(task: RootTask<*, *>) {
//
//        TODO("Not yet refactored")
//
////        holder.onProcessStart(this)
////        CoroutineScope(CoroutineName("Listener")).launch {
////            task.notifier.collect { notification ->
////                holder.onNotification(notification)
////            }
////        }
//    }
//    fun stopTaskObservation(task: RootTask<*, *>) {
//        val job = listenerJobs[task.key]
//        job?.invokeOnCompletion {
//            listenerJobs.remove(task.key)
//        }
//        holder.onProcessEnd(this)
//    }
//    override val key: CoroutineContext.Key<LoggProcess<*>> = Key
//    companion object Key : CoroutineContext.Key<LoggProcess<*>>
//}


