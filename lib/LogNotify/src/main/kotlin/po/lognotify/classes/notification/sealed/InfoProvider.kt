package po.lognotify.classes.notification.sealed

import po.lognotify.classes.process.LoggProcess
import po.lognotify.classes.task.ResultantTask
import po.lognotify.models.TaskDispatcher
import po.misc.exceptions.CoroutineInfo
import po.misc.time.ExecutionTimeStamp


data class ProviderTask(val task: ResultantTask) :DataProvider(){

    override val name: String = task.key.taskName
    override val id: String = task.key.taskId.toString()
    override val module: String = task.key.moduleName?:"N/A"
    override val nestingLevel: Int = task.key.nestingLevel
    override val coroutineInfo: CoroutineInfo =  CoroutineInfo.createInfo(task.coroutineContext)
    override var executionTime : ExecutionTimeStamp? = task.executionTimeStamp
}

data class ProviderProcess(val process: LoggProcess<*>) :DataProvider(){
    override val name: String = process.name
    override val id: String = process.identifiedAs
    override val module: String = "N/A"
    override val nestingLevel: Int = 0
    override val coroutineInfo: CoroutineInfo = process.coroutineInfo
    override var executionTime : ExecutionTimeStamp? = process.executionTimeStamp
}

data class ProviderLogNotify(
    val dispatcher: TaskDispatcher,
    override val  coroutineInfo: CoroutineInfo? = null
) :DataProvider()
{
    override val name: String = "LogNotify"
    override val id: String = "0"
    override val module: String = "LogNotify"
    override val nestingLevel: Int = 0
    override var executionTime : ExecutionTimeStamp? = null
}


sealed class DataProvider(){

    abstract val name : String
    abstract val id : String
    abstract val module: String
    abstract val nestingLevel : Int
    abstract val coroutineInfo : CoroutineInfo?
    abstract var executionTime : ExecutionTimeStamp?

}