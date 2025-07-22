package po.lognotify.classes.notification

import po.lognotify.classes.notification.models.LifecycleDataGroup
import po.lognotify.classes.notification.models.LogEvent
import po.lognotify.classes.notification.models.NotifyConfig
import po.lognotify.classes.notification.models.TaskData
import po.lognotify.common.LogInstance
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.Task
import po.lognotify.tasks.TaskBase
import po.lognotify.enums.SeverityLevel
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableTemplateBase
import po.misc.data.printable.TemplateAuxParams
import po.misc.data.printable.Printable
import po.misc.data.printable.PrintableCompanion
import po.misc.data.processors.DataProcessorBase
import po.misc.data.processors.FlowEmitter
import po.misc.data.styles.SpecialChars
import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwableToText
import po.misc.exceptions.waypointInfo

class LoggerDataProcessor(
    val task : TaskBase<*, *>,
    parent: LoggerDataProcessor?,
    emitter: FlowEmitter<TaskData>?,
) : DataProcessorBase<TaskData>(parent, emitter), LoggerContract {

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
           NotifyConfig.ConsoleBehaviour.Mute ->true
          NotifyConfig.ConsoleBehaviour.FullPrint -> false
          NotifyConfig.ConsoleBehaviour.MuteInfo -> data.severity == SeverityLevel.INFO
          NotifyConfig.ConsoleBehaviour.MuteNoEvents -> {
                //Should refactor later to lookup for events in the task chain
                false
            }
        }
    }

    var activeGroup: LifecycleDataGroup? = null

    private fun createGroup(logData: TaskData){
        activeGroup = LifecycleDataGroup(logData)
    }

    private fun createData(message: String, severity: SeverityLevel):TaskData{
        val data =  TaskData(
            taskHeader = task.header,
            config = task.config,
            timeStamp = task.executionTimeStamp,
            message = message,
            severity = severity,
            executionStatus = task.executionStatus,
            taskFooter = task.header

        )
        return data
    }

    private fun createData(arbitraryData: PrintableBase<*>, severity: SeverityLevel): TaskData{
        val taskData = createData("", severity)
        taskData.addChild(arbitraryData)
        return taskData
    }

    private fun createLogEvent(message: String, severity: SeverityLevel, logInstance: LogInstance<*>): LogEvent{
        val event = LogEvent(logInstance, message, severity)
        activeGroup?.let {
            it.processChild { group->
               if(config.console == NotifyConfig.ConsoleBehaviour.MuteNoEvents){
                  if(group.childCount > 0){
                      echo()
                  }
               }else{
                   echo()
               }
            }
        }?:run {

        }
       return LogEvent(logInstance, message, severity)
    }

    @PublishedApi
    internal fun errorHandled(handledBy: String, exception: ManagedException): TaskData {
        var message = "Exception: ${exception.throwableToText()} handled by $handledBy block in $task"
        message += SpecialChars.NewLine
        message += exception.waypointInfo()
        val dataRecord =  createData(message, SeverityLevel.EXCEPTION)
        processRecord(dataRecord, TaskData.Message)
        return dataRecord
    }

    @PublishedApi
    internal fun registerStart(): TaskData{
       val dataRecord = createData("", SeverityLevel.INFO)
        createGroup(dataRecord)
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
        if(config.debugAll == NotifyConfig.DebugOptions.DebugAll){
            processRecord(data, TaskData.Message)
            forwardOrEmmit(data)
        }else{
            debugData(data, TaskData, TaskData.Message){debuggable->
                processRecord(debuggable, TaskData.Message)
                forwardOrEmmit(debuggable)
            }
        }
    }

    private fun <T: PrintableBase<T>> debugRecord(arbitraryRecord: T){
        val actionSpan = task.activeActionSpan()
        val template = TaskData.Message
        val template2 = arbitraryRecord.defaultTemplate
        if (actionSpan != null) {
            template.setAuxParams(TemplateAuxParams(actionSpan.toString()))
        }
        val packedRecord = createData(arbitraryRecord, SeverityLevel.DEBUG)
        processRecord(packedRecord, template)
        forwardOrEmmit(packedRecord)
    }

    fun <T: PrintableBase<T>> debug(arbitraryRecord: T, arbitraryClass: PrintableCompanion<T>,  template: PrintableTemplateBase<T>?):T {
        if (config.debugAll == NotifyConfig.DebugOptions.DebugAll) {
            debugRecord(arbitraryRecord)
        } else {
            debugData(arbitraryRecord, arbitraryClass, template) { debuggable ->
                debugRecord(debuggable)
            }
        }
        return arbitraryRecord
    }

    fun <T: Printable> logFormatted(data: T, printFn: T.(StringBuilder)-> Unit){
        buildString{ data.printFn(this) }
        val asPrintable =  data as PrintableBase<*>
        asPrintable.echo()
        forwardOrEmmit(createData("Forwarding", SeverityLevel.LOG))
    }

    internal fun notify(message: String, severity: SeverityLevel): TaskData{
        val dataRecord = createData(message, severity)
        activeGroup?.let {
           val event =  createLogEvent(message, severity, task)
            it.addRecord(event)
        }
        processRecord(dataRecord, TaskData.Message)
        return dataRecord
    }

    override fun info(message: String): TaskData {
        val dataRecord = createData(message, SeverityLevel.INFO)
        processRecord(dataRecord, TaskData.Message)
        return dataRecord
    }

    override fun warn(message: String): TaskData {
        val dataRecord =createData(message, SeverityLevel.WARNING)
        processRecord(dataRecord, TaskData.Message)
        return dataRecord
    }

    fun warn(th: Throwable, message: String): TaskData {
        val dataRecord = createData("$message ${th.throwableToText()}", SeverityLevel.WARNING)
        processRecord(dataRecord, TaskData.Message)
        return dataRecord
    }

    fun error(exception: ManagedException){
        val text = "Exception: ${exception.message}. ${exception.waypointInfo()}"
        val dataRecord = createData(text, SeverityLevel.EXCEPTION)
        processRecord(dataRecord, TaskData.Message)
    }
}