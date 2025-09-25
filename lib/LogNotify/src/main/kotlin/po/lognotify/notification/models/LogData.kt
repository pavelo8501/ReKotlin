package po.lognotify.notification.models

import po.lognotify.common.configuration.TaskConfig
import po.lognotify.tasks.ExecutionStatus
import po.misc.data.helpers.applyIfNotEmpty
import po.misc.data.helpers.stripAfter
import po.misc.data.helpers.toStringIfNotNull
import po.misc.data.json.JsonDescriptor
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableGroup
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.Template
import po.misc.data.printable.companion.nextLine
import po.misc.data.printable.json.JsonReady
import po.misc.data.processors.SeverityLevel
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.exceptions.models.StackFrameMeta
import po.misc.exceptions.toStackTraceFormat
import po.misc.functions.dsl.helpers.nextBlock


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
                            SeverityLevel.INFO -> message.colorize(Colour.Green)
                            SeverityLevel.WARNING -> message.colorize(Colour.Yellow)
                            SeverityLevel.EXCEPTION -> message.colorize(Colour.Red)
                            SeverityLevel.DEBUG -> message.colorize(Colour.Magenta)
                        }
                    "${emitterName.applyIfNotEmpty { colorize(Colour.Cyan) + " -> " }}$message"
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
        val Default: Template<ErrorRecord> = createTemplate {
            nextLine {
                message.colorize(Colour.Red)
            }
            nextLine {
                "First registered: $firstRegisteredInTask"
            }
            nextLine {
                methodThrowing.toStringIfNotNull {"MethodThrowing: ".colorize(Colour.Yellow)+ it.toStackTraceFormat() }
            }
            nextLine {
                val callSite = throwingCallSite
                if (callSite != null) {
                    if (callSite.methodName == "invoke") {
                        """
                            ${Colour.makeOfColour(Colour.Yellow, "ThrowingCallSite: (actual exception place)")}
                            ${Colour.makeOfColour(Colour.Gray, "Class Name:")} ${callSite.fileName.stripAfter('$')}
                            ${Colour.makeOfColour(Colour.Gray, "Method Name:")
                        } ${callSite.methodName} (Lambda invocation)"
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
class DebugRecords(taskData: LogData): PrintableGroup<LogData, DebugData>(taskData, LogData.Header, DebugData.Default)

class LogData(
    var executionStatus: ExecutionStatus,
    val taskHeader: String,
    val config: TaskConfig
) : PrintableBase<LogData>(this), JsonReady<LogData> {
    override val self: LogData = this

    var elapsed: String = "N/A"
    var taskFooter: String = "N/A"

    val events: TaskEvents = TaskEvents(this)
    val errors: TaskErrors = TaskErrors(this)
    val debugRecords: DebugRecords = DebugRecords(this)

    val taskEvents: List<TaskEvent> get() = events.records

    val overallSeverity: SeverityLevel
        get() {
            if (errors.records.isNotEmpty()) {
                return SeverityLevel.EXCEPTION
            }
            return events.records
                .map { it.severity }
                .maxByOrNull { it.level } ?: SeverityLevel.INFO
        }

    override fun toString(): String = formattedString

    companion object : PrintableCompanion<LogData>({ LogData::class }) {
        val Header: Template<LogData> = createTemplate {
            nextBlock { handler ->
                handler.applyToResult { row -> "[ $row ]".colorize(Colour.Blue) }
                "Start $taskHeader"
            }
        }

        val Footer: Template<LogData> = createTemplate {
            nextBlock {
                val status = when (executionStatus) {
                    ExecutionStatus.Complete -> executionStatus.name.colorize(Colour.Green)
                    ExecutionStatus.Active -> executionStatus.name.colorize(Colour.WhiteBright)
                    ExecutionStatus.Failing, ExecutionStatus.Faulty -> executionStatus.name.colorize(Colour.Red)
                }
               "${Colour.makeOfColour(Colour.Blue, "[Stop $taskFooter | Status:")} $status ${Colour.makeOfColour(Colour.Blue, "]")}"
            }
        }

        val json: JsonDescriptor<LogData> = buildJsonDescriptor {
            createRecord(LogData::taskHeader)
            createRecord(LogData::executionStatus)
            createObject(LogData::taskEvents, TaskEvent::message)
        }

//        val json: JsonDescriptor<LogData> = JsonDescriptor<LogData>(this){
//
//            createObject(LogData::taskHeader, LogData::executionStatus, LogData::elapsed, LogData::taskFooter)
//            buildSubArray(TaskEvent::class, LogData::taskEvents){
//                createObject(TaskEvent::severity, TaskEvent::message)
//            }
//        }
    }
}
