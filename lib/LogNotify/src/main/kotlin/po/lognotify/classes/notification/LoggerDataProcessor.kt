package po.lognotify.classes.notification

import po.lognotify.classes.notification.models.ConsoleBehaviour
import po.lognotify.classes.notification.models.NotifyConfig
import po.lognotify.classes.notification.models.TaskData
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.Task
import po.lognotify.classes.task.TaskBase
import po.lognotify.enums.SeverityLevel
import po.misc.data.printable.PrintableBase
import po.misc.data.console.PrintableTemplateBase
import po.misc.data.printable.Printable
import po.misc.data.printable.PrintableCompanion
import po.misc.data.processors.DataProcessorBase
import po.misc.data.processors.FlowEmitter
import po.misc.data.styles.SpecialChars
import po.misc.exceptions.ManagedException
import po.misc.exceptions.name
import po.misc.exceptions.waypointInfo

class LoggerDataProcessor(
    val task : TaskBase<*, *>,
    parent: LoggerDataProcessor?,
    emitter: FlowEmitter<TaskData>?,
) : DataProcessorBase<TaskData>(parent, emitter) {

    enum class LoggerProcessorType{RootTask, Task }
    var config : NotifyConfig
    var processorType: LoggerProcessorType = LoggerProcessorType.Task

    init {
        when(task){
            is RootTask -> {
                processorType = LoggerProcessorType.RootTask
                config =  task.dispatcher.notifierHub.sharedConfig
            }
            is Task->{
              processorType = LoggerProcessorType.Task
              config =  task.hierarchyRoot.dataProcessor.config
            }
        }
        updateDebugWhiteList(config.debugWhiteList)
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

    private fun createData(message: String, severity: SeverityLevel):TaskData{
        val data =  TaskData(
            taskKey = task.key,
            config = task.config,
            timeStamp = task.executionTimeStamp,
            message = message,
            severity = severity
        )
        return data
    }

    private fun createData(arbitraryData: PrintableBase<*>, severity: SeverityLevel):TaskData{
        val data =  TaskData(
            taskKey = task.key,
            config = task.config,
            timeStamp = task.executionTimeStamp,
            message = arbitraryData.formattedString,
            severity = severity
        )
        data.addChild(arbitraryData)
        return data
    }

    @PublishedApi
    internal fun errorHandled(handledBy: String, exception: ManagedException): TaskData {
        var message = "Exception: ${exception.name()} handled by $handledBy block in $task"
        message += SpecialChars.NewLine
        message += exception.waypointInfo()
        val dataRecord =  createData(message, SeverityLevel.EXCEPTION)
        processRecord(dataRecord, TaskData.Exception)
        return dataRecord
    }

    @PublishedApi
    internal fun registerStart(): TaskData{
       val dataRecord = createData("", SeverityLevel.INFO)
        processRecord(dataRecord, TaskData.Header)
        forwardOrEmmit(dataRecord)
        return dataRecord
    }

    @PublishedApi
    internal  fun registerStop():TaskData{
        val dataRecord = createData("", SeverityLevel.INFO)
        processRecord(dataRecord, TaskData.Footer)
        forwardOrEmmit(dataRecord)
        emitter?.stopBroadcast()
        return dataRecord
    }

    fun <T: PrintableBase<T>> log(arbitraryRecord: T, template: PrintableTemplateBase<T>):T{
        arbitraryRecord.defaultTemplate = template
        arbitraryRecord.echo()
        val packedRecord = createData(arbitraryRecord, SeverityLevel.LOG)
        forwardOrEmmit(packedRecord)
        return arbitraryRecord
    }

    @PublishedApi
    internal fun debug(message: String, methodName: String){
        val data = createData("$message @ $methodName in $task", SeverityLevel.DEBUG)
        debugData(data, TaskData, TaskData.Debug){debuggable->
            processRecord(debuggable, TaskData.Debug)
        }
    }
    fun <T: PrintableBase<T>> debug(arbitraryRecord: T, arbitraryClass: PrintableCompanion<T>,  template: PrintableTemplateBase<T>):T{
        debugData(arbitraryRecord, arbitraryClass, template){debuggable->
            debuggable.echo()
            val packedRecord = createData(debuggable, SeverityLevel.DEBUG)
            forwardOrEmmit(packedRecord)
        }
       return arbitraryRecord
    }



    fun <T: Printable> logFormatted(data: T, printFn: T.(StringBuilder)-> Unit){
        buildString{ data.printFn(this) }
        val asPrintable =  data as PrintableBase<*>
        asPrintable.echo()
        forwardOrEmmit(createData("Forwarding", SeverityLevel.LOG))
    }

    fun info(message: String): TaskData {
        val dataRecord = createData(message, SeverityLevel.INFO)
        processRecord(dataRecord, TaskData.Message)
        return dataRecord
    }

    fun warn(message: String): TaskData {
        val dataRecord =createData(message, SeverityLevel.WARNING)
        processRecord(dataRecord, TaskData.Message)
        return dataRecord
    }

    fun warn(th: Throwable, message: String): TaskData {
        return createData("$message ${th.message.toString()}", SeverityLevel.WARNING)
    }

    fun error(exception: ManagedException): TaskData {
       val dataRecord =  createData(exception.message.toString(), SeverityLevel.EXCEPTION)
        processRecord(dataRecord, TaskData.Exception)
        return dataRecord
    }
}