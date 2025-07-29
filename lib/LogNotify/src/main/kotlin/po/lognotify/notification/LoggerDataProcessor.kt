package po.lognotify.notification

import po.lognotify.action.ActionSpan
import po.lognotify.common.LNInstance
import po.lognotify.notification.models.LogEvent
import po.lognotify.notification.models.NotifyConfig
import po.lognotify.notification.models.TaskData
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.TaskBase
import po.lognotify.enums.SeverityLevel
import po.lognotify.notification.models.ConsoleBehaviour
import po.lognotify.notification.models.ErrorSnapshot
import po.lognotify.notification.models.ExceptionRecord
import po.lognotify.notification.models.TaskEvents
import po.misc.context.CTX
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableTemplateBase
import po.misc.data.printable.Printable
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.processors.DataProcessorBase
import po.misc.data.processors.FlowEmitter
import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwableToText

class LoggerDataProcessor(
    val task : TaskBase<*, *>,
    parent: LoggerDataProcessor?,
    emitter: FlowEmitter<TaskData>?,
) : DataProcessorBase<TaskData>(parent, emitter), LoggerContract {

    enum class LoggerProcessorType{RootTask, Task}

    val config: NotifyConfig get () = task.config.notifConfig
    val processorType: LoggerProcessorType get() {
      return  if(task is RootTask){
            LoggerProcessorType.RootTask
        }else{
            LoggerProcessorType.Task
        }
    }

    override val records: MutableList<TaskData> = mutableListOf()

    internal val taskData: TaskData = TaskData(
        taskHeader = task.header,
        config = task.config,
        timeStamp = task.executionTimeStamp,
        severity = SeverityLevel.INFO,
        executionStatus = task.executionStatus,
        taskFooter = task.footer
    )

    internal val taskEvents: TaskEvents get() = taskData.events

    init {

        records.add(taskData)

        taskEvents.onNewChild(::onNewLogEvent)
        if(config.console !=  ConsoleBehaviour.Mute && config.console != ConsoleBehaviour.MuteNoEvents){
            taskData.echo(TaskData.Header)
        }
        updateDebugWhiteList(config.debugWhiteList)
    }

    override fun onChildDataReceived(childRecords: List<TaskData>){
        records.addAll(childRecords)
    }

    private fun onNewLogEvent(logEvent: LogEvent){
        consoleOutput(logEvent, logEvent.severity)
    }

    private fun createLogEvent(message: String, severity: SeverityLevel, lnInstance: LNInstance<*>): LogEvent{
        val prefix = when(lnInstance){
            is TaskBase<*, *>-> ""
            is ActionSpan<*, *> -> lnInstance.header
            else -> "Unknown emitter"
        }
        return LogEvent(prefix, message, severity)
    }

    private fun consoleOutput(data: PrintableBase<*>, severity: SeverityLevel){
        when(config.console){
            ConsoleBehaviour.MuteNoEvents->{
                taskData.echo(TaskData.Header)
                data.echo()
            }
            ConsoleBehaviour.MuteInfo->{
                if(severity != SeverityLevel.INFO){
                    data.echo()
                }
            }
            else -> {
                data.echo()
            }
        }
    }

    @PublishedApi
    internal  fun registerStop():TaskData{
        if(config.console !=  ConsoleBehaviour.Mute && config.console != ConsoleBehaviour.MuteNoEvents){
            taskData.echo(TaskData.Header)
        }else if (config.console !=  ConsoleBehaviour.MuteNoEvents && taskEvents.records.isNotEmpty()){
            taskData.echo(TaskData.Footer)
        }
        forwardOrEmmit(taskData)
        flowEmitter?.stopBroadcast()
        return taskData
    }

    internal fun addExceptionRecord(snapshot : ErrorSnapshot, managed: ManagedException) {

        records.lastOrNull()?.events?.records?.lastOrNull()?.let { lastRecord ->
            val firstExRecord = managed.exceptionData.firstOrNull { it.event == ManagedException.ExceptionEvent.Thrown }
            if (firstExRecord != null) {
                val record = ExceptionRecord(
                    message = managed.throwableToText(),
                    firstRegisteredInTask = snapshot.taskHeader,
                    methodThrowing = firstExRecord.stackTrace.firstOrNull(),
                    throwingCallSite = firstExRecord.stackTrace.getOrNull(1),
                    actionSpans = snapshot.actionRecords
                )
                lastRecord.exceptionRecord = record
            }
        }
    }
    internal fun log(message: String, severity: SeverityLevel, lnInstance: LNInstance<*>): LogEvent{
        val logEvent = createLogEvent(message, severity, lnInstance)
        taskEvents.addRecord(logEvent)
        return logEvent
    }

    internal fun notify(message: String, severity: SeverityLevel): TaskData{
        log(message, severity, task)
        return taskData
    }

    fun notify(message: String, severity: SeverityLevel, receiver: CTX): TaskData{
        val logEvent = createLogEvent(message, severity, task)
        taskEvents.addRecord(logEvent.setArbitraryContext(receiver))
        return taskData
    }

    override fun info(message: String): TaskData {
        log(message, SeverityLevel.INFO, task)
        return taskData
    }
    override fun warn(message: String): TaskData {
        log(message, SeverityLevel.WARNING, task)
        return taskData
    }

    fun warn(th: Throwable, message: String): TaskData {
        log("$message ${th.throwableToText()}", SeverityLevel.WARNING, task)
        return taskData
    }

    fun <T: PrintableBase<T>> log(arbitraryRecord: T, severity: SeverityLevel):T{
        consoleOutput(arbitraryRecord, severity)
        taskData.arbitraryData.add(arbitraryRecord)
        return arbitraryRecord
    }

    fun <T: PrintableBase<T>> log(arbitraryRecord: T):T{
        consoleOutput(arbitraryRecord, SeverityLevel.LOG)
        taskData.arbitraryData.add(arbitraryRecord)
        return arbitraryRecord
    }

    @PublishedApi
    internal fun debug(message: String, methodName: String){
        val logEvent =  createLogEvent("$message @ $methodName in $task", SeverityLevel.DEBUG, task)
    }

    private fun <T: PrintableBase<T>> debugRecord(arbitraryRecord: T){
        val actionSpan = task.activeActionSpan()
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
       // forwardOrEmmit(createData("Forwarding", SeverityLevel.LOG))
    }

}