package po.lognotify.notification.models

import po.lognotify.common.configuration.TaskConfig
import po.lognotify.tasks.ExecutionStatus
import po.misc.context.CTX
import po.misc.data.helpers.textIfNotNull
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableGroup
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.Template
import po.misc.data.printable.companion.nextLine
import po.misc.data.processors.SeverityLevel
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.data.text.applyIfNotEmpty
import po.misc.data.text.stripAfter
import po.misc.exceptions.models.StackFrameMeta
import po.misc.exceptions.toStackTraceFormat
import po.misc.functions.dsl.helpers.nextBlock
import po.misc.time.ExecutionTimeStamp


class TaskEvent(
    var emitterName: String,
    val message: String,
    val severity: SeverityLevel,
) : PrintableBase<TaskEvent>(this) {
    override val self: TaskEvent = this

    companion object : PrintableCompanion<TaskEvent>({ TaskEvent::class }) {
        val Message: Template<TaskEvent> =
            createTemplate {
                nextBlock {
                    val message =
                        when (severity) {
                            SeverityLevel.INFO -> message.colorize(Colour.GREEN)
                            SeverityLevel.WARNING -> message.colorize(Colour.Yellow)
                            SeverityLevel.EXCEPTION -> message.colorize(Colour.RED)
                            SeverityLevel.DEBUG -> message.colorize(Colour.MAGENTA)
                        }
                    "${emitterName.applyIfNotEmpty { colorize(Colour.CYAN) + " -> " }}$message"
                }
            }
    }
}

class TaskEvents(
    taskData: LogData,
) : PrintableGroup<LogData, TaskEvent>(taskData, LogData.Header, TaskEvent.Message) {
    init {
        setFooter(LogData.Footer)
    }
}

class ErrorRecord(
    val message: String,
    val firstRegisteredInTask: String,
    val methodThrowing: StackFrameMeta?,
    val throwingCallSite: StackFrameMeta?,
    val actionSpans: List<ActionData>? = null,
) : PrintableBase<ErrorRecord>(this) {
    override val self: ErrorRecord = this

    companion object : PrintableCompanion<ErrorRecord>({ ErrorRecord::class }) {
        val Default: Template<ErrorRecord> =
            createTemplate {
                nextLine {
                    message.colorize(Colour.RED)
                }
                nextLine {
                    "First registered: $firstRegisteredInTask"
                }
                nextLine {
                    methodThrowing.textIfNotNull<StackFrameMeta> { "MethodThrowing: ".colorize(Colour.Yellow) + toStackTraceFormat() }
                }
                nextLine {
                    val callSite = throwingCallSite
                    if (callSite != null) {
                        if (callSite.methodName == "invoke") {
                            """
                            ${Colour.makeOfColour(Colour.Yellow, "ThrowingCallSite: (actual exception place)")}
                            ${Colour.makeOfColour(Colour.Gray, "Class Name:")} ${callSite.className.stripAfter('$')}
                            ${Colour.makeOfColour(Colour.Gray, "Method Name:")} ${callSite.methodName} (Lambda invocation)"
                            ${Colour.makeOfColour(Colour.Gray, "Reference:")}
                            ${callSite.toStackTraceFormat()}
                            """.trimIndent()
                        } else {
                            "ThrowingCallSite: (actual exception place)".colorize(Colour.Yellow) + callSite.toStackTraceFormat()
                        }
                    } else {
                        SpecialChars.Empty.char
                    }
                }
            }
    }
}

class TaskErrors(taskData: LogData): PrintableGroup<LogData, ErrorRecord>(taskData, LogData.Header, ErrorRecord.Default)


class LogData(
    val executionStatus: ExecutionStatus,
    val taskHeader: String,
    val taskFooter: String,
    val config: TaskConfig,
    val timeStamp: ExecutionTimeStamp,
) : PrintableBase<LogData>(this) {
    override val self: LogData = this

    val events: TaskEvents = TaskEvents(this)
    val errors: TaskErrors = TaskErrors(this)

    val overallSeverity: SeverityLevel get(){
        if(errors.records.isNotEmpty()){
            return SeverityLevel.EXCEPTION
        }
        return events.records
            .map { it.severity }
            .maxByOrNull { it.level } ?: SeverityLevel.INFO
    }

    override fun toString(): String = formattedString

    companion object : PrintableCompanion<LogData>({ LogData::class }) {
        val Header: Template<LogData> =
            createTemplate {
                nextBlock { handler ->
                    handler.applyToResult { row -> "[ $row ]" }
                    val status =
                        when (executionStatus) {
                            ExecutionStatus.Complete -> executionStatus.name.colorize(Colour.GREEN)
                            ExecutionStatus.Active -> executionStatus.name.colorize(Colour.BRIGHT_WHITE)
                            ExecutionStatus.Failing, ExecutionStatus.Faulty -> executionStatus.name.colorize(Colour.RED)
                        }
                    "$taskHeader | Status: ".colorize(Colour.BLUE) + status + "".colorize(Colour.BLUE)
                }
            }
        val Footer: Template<LogData> =
            createTemplate {
                nextBlock { handler ->
                    handler.applyToResult { row -> "[ $row ]" }
                    taskFooter.colorize(Colour.BLUE)
                }
            }
    }
}
