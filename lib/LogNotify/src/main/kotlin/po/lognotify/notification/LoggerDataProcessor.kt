package po.lognotify.notification

import po.lognotify.action.ActionSpan
import po.lognotify.common.LNInstance
import po.lognotify.notification.models.ConsoleBehaviour
import po.lognotify.notification.models.ErrorSnapshot
import po.lognotify.notification.models.ExceptionRecord
import po.lognotify.notification.models.LogEvent
import po.lognotify.notification.models.NotifyConfig
import po.lognotify.notification.models.TaskData
import po.lognotify.notification.models.TaskEvents
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.TaskBase
import po.misc.context.CTX
import po.misc.data.printable.Printable
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.PrintableTemplateBase
import po.misc.data.processors.DataProcessorBase
import po.misc.data.processors.FlowEmitter
import po.misc.data.processors.SeverityLevel
import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwableToText

class LoggerDataProcessor(
    val task: TaskBase<*, *>,
    parent: LoggerDataProcessor?,
    emitter: FlowEmitter<TaskData>?,
) : DataProcessorBase<TaskData>(parent, emitter),
    LogDataProcessorContract {
    enum class LoggerProcessorType { RootTask, Task }

    val config: NotifyConfig get() = task.config.notifConfig
    val processorType: LoggerProcessorType get() {
        return if (task is RootTask) {
            LoggerProcessorType.RootTask
        } else {
            LoggerProcessorType.Task
        }
    }

    override val records: MutableList<TaskData> = mutableListOf()

    internal val taskData: TaskData =
        TaskData(
            taskHeader = task.header,
            config = task.config,
            timeStamp = task.executionTimeStamp,
            severity = SeverityLevel.INFO,
            executionStatus = task.executionStatus,
            taskFooter = task.footer,
        )

    internal val taskEvents: TaskEvents get() = taskData.events

    init {
        records.add(taskData)
        taskEvents.onNewChild(::onNewLogEvent)
        if (config.console != ConsoleBehaviour.Mute && config.console != ConsoleBehaviour.MuteNoEvents) {
            taskData.echo(TaskData.Header)
        }
        updateDebugWhiteList(config.debugWhiteList)
    }

    override fun onChildDataReceived(childRecords: List<TaskData>) {
        records.addAll(childRecords)
    }

    private fun onNewLogEvent(logEvent: LogEvent) {
        consoleOutput(logEvent, logEvent.severity)
    }

    private fun createLogEvent(
        message: String,
        severity: SeverityLevel,
        lnInstance: LNInstance<*>,
    ): LogEvent {
        val prefix =
            when (lnInstance) {
                is TaskBase<*, *> -> ""
                is ActionSpan<*, *> -> lnInstance.header
                else -> "Unknown emitter"
            }
        return LogEvent(prefix, message, severity)
    }

    private fun consoleOutput(
        data: PrintableBase<*>,
        severity: SeverityLevel,
    ) {
        when (config.console) {
            ConsoleBehaviour.MuteNoEvents -> {
                taskData.echo(TaskData.Header)
                data.echo()
            }
            ConsoleBehaviour.MuteInfo -> {
                if (severity != SeverityLevel.INFO) {
                    data.echo()
                }
            }
            else -> data.echo()
        }
    }

    @PublishedApi
    internal fun registerStop(): TaskData {
        if (config.console != ConsoleBehaviour.Mute && config.console != ConsoleBehaviour.MuteNoEvents) {
            taskData.echo(TaskData.Header)
        } else if (config.console != ConsoleBehaviour.MuteNoEvents && taskEvents.records.isNotEmpty()) {
            taskData.echo(TaskData.Footer)
        }
        taskEvents.echo()
        forwardOrEmmit(taskData)
        flowEmitter?.stopBroadcast()
        return taskData
    }

    internal fun addExceptionRecord(
        snapshot: ErrorSnapshot,
        managed: ManagedException,
    ) {
        records.lastOrNull()?.events?.records?.lastOrNull()?.let { lastRecord ->
            val firstExRecord = managed.exceptionData.firstOrNull { it.event == ManagedException.ExceptionEvent.Thrown }
            if (firstExRecord != null) {
                val record =
                    ExceptionRecord(
                        message = managed.throwableToText(),
                        firstRegisteredInTask = snapshot.taskHeader,
                        methodThrowing = firstExRecord.stackTrace.firstOrNull(),
                        throwingCallSite = firstExRecord.stackTrace.getOrNull(1),
                        actionSpans = snapshot.actionRecords,
                    )
                record.echo()
                lastRecord.exceptionRecord = record
            }
        }
    }

    internal fun notify(
        message: String,
        severity: SeverityLevel,
        lnInstance: LNInstance<*>,
    ): LogEvent {
        val logEvent = createLogEvent(message, severity, lnInstance)
        taskEvents.addRecord(logEvent)
        return logEvent
    }

    override fun log(
        arbitraryRecord: PrintableBase<*>,
        severity: SeverityLevel,
        emitter: Any,
    ) {
        consoleOutput(arbitraryRecord, severity)
        taskData.arbitraryData.add(arbitraryRecord)
    }

    override fun notify(
        message: String,
        severity: SeverityLevel,
        emitter: Any,
    ) {
        val logEvent = createLogEvent(message, severity, task)
        when (emitter) {
            is CTX -> taskEvents.addRecord(logEvent.setArbitraryContext(emitter))
        }
    }

    @PublishedApi
    internal fun debug(
        message: String,
        methodName: String,
    ) {
        val logEvent = createLogEvent("$message @ $methodName in $task", SeverityLevel.DEBUG, task)
    }

    private fun <T : PrintableBase<T>> debugRecord(arbitraryRecord: T) {
        val actionSpan = task.activeActionSpan()
    }

    fun <T : PrintableBase<T>> debug(
        arbitraryRecord: T,
        arbitraryClass: PrintableCompanion<T>,
        template: PrintableTemplateBase<T>?,
    ): T {
        if (config.debugAll == NotifyConfig.DebugOptions.DebugAll) {
            debugRecord(arbitraryRecord)
        } else {
            debugData(arbitraryRecord, arbitraryClass, template) { debuggable ->
                debugRecord(debuggable)
            }
        }
        return arbitraryRecord
    }

    fun <T : Printable> logFormatted(
        data: T,
        printFn: T.(StringBuilder) -> Unit,
    ) {
        buildString { data.printFn(this) }
        val asPrintable = data as PrintableBase<*>
        asPrintable.echo()
        // forwardOrEmmit(createData("Forwarding", SeverityLevel.LOG))
    }
}
