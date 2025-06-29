package po.lognotify.classes.notification

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.models.ConsoleBehaviour
import po.lognotify.classes.notification.models.NotifyConfig
import po.lognotify.classes.notification.models.TaskData
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.Task
import po.lognotify.classes.task.TaskBase
import po.lognotify.enums.SeverityLevel
import po.misc.data.PrintableBase
import po.misc.data.console.PrintableTemplateBase
import po.misc.data.interfaces.Printable
import po.misc.data.processors.DataProcessorBase
import po.misc.data.styles.SpecialChars
import po.misc.exceptions.ManagedException
import po.misc.exceptions.name
import po.misc.exceptions.waypointInfo

class LoggerDataProcessor(
    val task : TaskBase<*, *>,
    override val topEmitter: LoggerDataProcessor?
) : DataProcessorBase<TaskData>() {

    enum class LoggerProcessorType{RootTask, Task }
    var config : NotifyConfig = NotifyConfig()
    var processorType: LoggerProcessorType = LoggerProcessorType.Task

    init {
        when(task){
            is RootTask -> {
                processorType = LoggerProcessorType.RootTask
            }
            is Task->{
              processorType = LoggerProcessorType.Task
              config =  task.hierarchyRoot.dataProcessor.config
            }
        }
        provideMuteCondition(::setMuteConditions)
    }

    private fun setMuteConditions(data: TaskData): Boolean{
      return when(config.console){
            ConsoleBehaviour.Mute ->true
            ConsoleBehaviour.FullPrint -> false
            ConsoleBehaviour.MuteInfo -> data.severity == SeverityLevel.INFO
            ConsoleBehaviour.MuteNoEvents -> {
                //Should refactor later to lookup for events in the task chain
                false
            }
        }
    }

    private fun forwardOrEmmit(data:TaskData){
        if(task is RootTask){
            emitData(data)
        }else{
            forwardTop(data)
        }
    }

    private fun createData(message: String, severity: SeverityLevel, arbitraryData: PrintableBase<*>?):TaskData{
        val data =  TaskData(
            taskKey = task.key,
            config = task.config,
            timeStamp = task.executionTimeStamp,
            message = message,
            severity = severity
        )
        return data
    }

    fun registerStart(): TaskData{
       val dataRecord = createData("", SeverityLevel.INFO, null)
        processRecord(dataRecord, TaskData.Header)
        forwardOrEmmit(dataRecord)
        return dataRecord
    }

    fun registerStop():TaskData{
        val dataRecord = createData("", SeverityLevel.INFO, null)
        processRecord(dataRecord, TaskData.Footer)
        forwardOrEmmit(dataRecord)
        stopBroadcast()
        return dataRecord
    }

    fun <T: PrintableBase<T>> log(dataRecord: T, template: PrintableTemplateBase<T>){
        dataRecord.defaultTemplate = template
        dataRecord.echo()
        val packedRecord = createData("Forwarding", SeverityLevel.LOG, dataRecord)
        processRecord(packedRecord, null)
        forwardOrEmmit(packedRecord)
    }

    fun <T: Printable> logFormatted(data: T, printFn: T.(StringBuilder)-> Unit){
        buildString{ data.printFn(this) }
        val asPrintable =  data as PrintableBase<*>
        asPrintable.echo()
        forwardOrEmmit(createData("Forwarding", SeverityLevel.LOG, asPrintable))
    }

    fun info(message: String): TaskData {
        val dataRecord = createData(message, SeverityLevel.INFO, null)
        processRecord(dataRecord, TaskData.Message)
        return dataRecord
    }
    fun <R> info(message: String, task: TaskBase<*, R>): TaskData{
        val dataRecord = createData(message, SeverityLevel.INFO, null)
        processRecord(dataRecord, TaskData.Message)
        return dataRecord
    }

    fun warn(message: String): TaskData {
        val dataRecord =createData(message, SeverityLevel.WARNING,  null)
        processRecord(dataRecord, TaskData.Message)
        return dataRecord
    }

    fun error(exception: ManagedException): TaskData {
       val dataRecord =  createData(exception.message.toString(), SeverityLevel.EXCEPTION, null)
        processRecord(dataRecord, TaskData.Exception)
        return dataRecord
    }

    @PublishedApi
    internal fun errorHandled(handledBy: String, exception: ManagedException): TaskData {
        var message = "Exception: ${exception.name()} handled by $handledBy block in $task"
        message += SpecialChars.NewLine
        message += exception.waypointInfo()
        val dataRecord =  createData(message, SeverityLevel.EXCEPTION, null)
        processRecord(dataRecord, TaskData.Exception)
        return dataRecord
    }

    internal fun <R> warn(message: String, task: TaskBase<*, R>): TaskData{
        return  createData(message, SeverityLevel.WARNING, null)
    }
    fun warn(th: Throwable, message: String): TaskData {
        return createData("$message ${th.message.toString()}", SeverityLevel.WARNING,  null)
    }
    @PublishedApi
    internal fun <R> debug(message: String, where: String,  task: TaskBase<*, R>){
        if(config.inShowDebugList(TaskData.Debug)){
            createData("Debug: $where -> $message" , SeverityLevel.DEBUG, null)
        }
    }
}