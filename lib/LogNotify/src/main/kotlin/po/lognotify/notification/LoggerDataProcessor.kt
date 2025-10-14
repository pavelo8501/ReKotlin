package po.lognotify.notification

import po.lognotify.action.ActionSpan
import po.lognotify.common.LNInstance
import po.lognotify.notification.models.ConsoleBehaviour
import po.lognotify.notification.models.DebugData
import po.lognotify.notification.models.ErrorRecord
import po.lognotify.notification.models.ErrorSnapshot
import po.lognotify.notification.models.NotifyConfig
import po.lognotify.notification.models.LogData
import po.lognotify.notification.models.TaskEvent
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.TaskBase
import po.misc.context.CTX
import po.misc.coroutines.HotFlowEmitter
import po.misc.data.helpers.output
import po.misc.data.printable.Printable
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.PrintableTemplateBase
import po.misc.data.printable.companion.Template
import po.misc.data.processors.DataProcessorBase
import po.misc.data.processors.SeverityLevel
import po.misc.debugging.DebugTopic
import po.misc.debugging.createDebugFrame
import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwableToText


class LoggerDataProcessor(
    val task: TaskBase<*, *>,
    parent: LoggerDataProcessor?
) : DataProcessorBase<LogData>(parent), LogDataProcessorContract {

    val config: NotifyConfig get() = task.config.notifConfig
    val isRoot: Boolean get() = task is RootTask

    var  flowEmitter: HotFlowEmitter<LogData>? = null

    internal val taskData: LogData =
        LogData(
            taskHeader = task.header,
            config = task.config,
            executionStatus = task.executionStatus,
        )

    init {
        if(isRoot){
            flowEmitter = HotFlowEmitter<LogData>()
        }
        addData(taskData)
        flowEmitter?.emitData(taskData)
        taskData.events.onNewRecord(::onNewTaskEvent)
        onSubDataReceived(::onSubLogReceived)

        if (config.console != ConsoleBehaviour.Mute && config.console != ConsoleBehaviour.MuteNoEvents) {
            taskData.echo(LogData.Header)
        }
        updateDebugWhiteList(config.debugWhiteList)
    }

    private fun onNewTaskEvent(taskEvent: TaskEvent) {
        consoleOutput(taskEvent, taskEvent.severity)
    }

    private fun onSubLogReceived(data: LogData, subProcessor:DataProcessorBase<LogData>) {
        flowEmitter?.emitData(data)
    }

    private fun createLogEvent(
        emitterName: String,
        message: String,
        severity: SeverityLevel,
    ): TaskEvent {
        return TaskEvent(emitterName, message, severity)
    }

    private fun consoleOutput(
        data: PrintableBase<*>,
        severity: SeverityLevel,
    ) {
        when (config.console) {
            ConsoleBehaviour.MuteNoEvents -> {
                taskData.echo(LogData.Header)
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
    internal fun registerStop(): LogData {
        taskData.executionStatus = task.executionStatus
        if (config.console != ConsoleBehaviour.Mute && config.console != ConsoleBehaviour.MuteNoEvents) {
            taskData.taskFooter = task.footer
            taskData.echo(LogData.Footer)
        } else if (config.console != ConsoleBehaviour.MuteNoEvents && taskData.events.records.isNotEmpty()) {
            taskData.echo(LogData.Footer)
        }
        flowEmitter?.stopBroadcast()
        return taskData
    }

    internal fun addErrorRecord(
        snapshot: ErrorSnapshot,
        managed: ManagedException,
    ) {
        val firstExRecord = managed.exceptionData.firstOrNull { it.event == ManagedException.ExceptionEvent.Thrown }
        if (firstExRecord != null) {
            val record =
                ErrorRecord(
                    message = managed.throwableToText(),
                    firstRegisteredInTask = snapshot.taskHeader,
                    methodThrowing = firstExRecord.stackTrace.firstOrNull(),
                    throwingCallSite = firstExRecord.stackTrace.getOrNull(1),
                    actionSpans = snapshot.actionRecords,
                )
            record.echo()
            taskData.errors.addRecord(record)
        }
    }

    override fun log(
        arbitraryRecord: PrintableBase<*>,
        severity: SeverityLevel,
        emitter: Any,
    ) {
        consoleOutput(arbitraryRecord, severity)
        taskData.addArbitraryRecord(arbitraryRecord)
    }

    override fun notify(emittingContext: Any, message: String, severity: SeverityLevel){
        val logEvent = when (emittingContext) {
            is LNInstance<*> -> {
                val emitterName = when(emittingContext) {
                    is TaskBase<*, *> -> ""
                    is ActionSpan<*, *> -> emittingContext.header
                    else -> "Unknown emitter"
                }
                createLogEvent(emitterName,  message, severity)
            }
            is CTX -> createLogEvent(emittingContext.identifiedByName,  message, severity)
            else -> createLogEvent(emittingContext::class.simpleName.toString(),  message, severity)
        }
        taskData.events.addRecord(logEvent)
    }

    fun <T: Printable> debug(
        message: String,
        callingContext: CTX,
        topic: DebugTopic = DebugTopic.General,
        template: PrintableTemplateBase<T>?
    ){
       val debugFrame = callingContext.createDebugFrame(methodName = "debug")
        debugFrame.frameMeta

        val newData = DebugData(
            message = message,
            contextName =  debugFrame.contextName,
            completeContextName =  debugFrame.contextName,
            stackMeta = debugFrame.frameMeta
        )
        newData.setTopic(topic)

        when(template){
            is PrintableTemplateBase<*> ->{
                newData.trySetDefaultTemplate(template)
            }
            else -> {
                newData.setDefaultTemplate(DebugData.Default)
            }
        }
        newData.echo()
        taskData.debugRecords.addRecord(newData)
    }

    @PublishedApi
    internal fun debug(
        message: String,
        methodName: String,
    ) {
        val logEvent = createLogEvent(task.header,"$message @ $methodName in $task", SeverityLevel.DEBUG)
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
    }

}
