package po.lognotify.notification.models

import po.lognotify.tasks.models.TaskConfig
import po.lognotify.tasks.ExecutionStatus
import po.misc.context.CTX
import po.misc.data.helpers.textIfNotNull
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.PrintableGroup
import po.misc.data.printable.companion.Template
import po.misc.data.printable.companion.nextLine
import po.misc.data.processors.SeverityLevel
import po.misc.data.styles.colorize
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.text.applyIfNotEmpty
import po.misc.data.text.stripAfter
import po.misc.exceptions.models.StackFrameMeta
import po.misc.exceptions.toStackTraceFormat
import po.misc.functions.dsl.helpers.nextBlock
import po.misc.time.ExecutionTimeStamp


class TaskEvents(
    taskData: TaskData,
): PrintableGroup<TaskData, LogEvent>(taskData, TaskData.Header, LogEvent.Message){
    init {
        setFooter(TaskData.Footer)
    }
}


class LogEvent(
    val prefix: String,
    val message: String,
    val severity: SeverityLevel
): PrintableBase<LogEvent>(this){
    override val self: LogEvent = this

    var arbitraryContext: CTX? = null
        private set

    var exceptionRecord:ExceptionRecord? = null

    fun setArbitraryContext(context: CTX):LogEvent{
        arbitraryContext = context
        return  this
    }


    companion object: PrintableCompanion<LogEvent>({LogEvent::class}) {

        val Message : Template<LogEvent> = createTemplate {
            nextBlock {
               val message = when(severity){
                    SeverityLevel.INFO, SeverityLevel.LOG -> message.colorize(Colour.GREEN)
                    SeverityLevel.WARNING -> message.colorize(Colour.Yellow)
                    SeverityLevel.EXCEPTION-> message.colorize(Colour.RED)
                    SeverityLevel.DEBUG -> message.colorize(Colour.MAGENTA)
                }
               "${prefix.applyIfNotEmpty { colorize(Colour.CYAN) + " -> " }}$message"
            }
            nextBlock {
                exceptionRecord?.formattedString?:""
            }
        }
    }
}


class ExceptionRecord(
    val message: String,
    val firstRegisteredInTask: String,
    val methodThrowing: StackFrameMeta?,
    val throwingCallSite:StackFrameMeta?,
    val actionSpans: List<ActionData>? = null
): PrintableBase<ExceptionRecord>(this) {

    override val self: ExceptionRecord = this

    companion object : PrintableCompanion<ExceptionRecord>({ ExceptionRecord::class }) {
        val Default: Template<ExceptionRecord> = createTemplate {

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


class TaskData(
    val executionStatus: ExecutionStatus,
    val taskHeader: String,
    val taskFooter: String,
    val config: TaskConfig,
    val timeStamp: ExecutionTimeStamp,
    val severity: SeverityLevel = SeverityLevel.INFO,
) : PrintableBase<TaskData>(this) {
    override val self: TaskData = this

    val events:TaskEvents = TaskEvents(this)
    val arbitraryData: MutableList<PrintableBase<*>> = mutableListOf()


    companion object : PrintableCompanion<TaskData>({ TaskData::class }){

        val Header: Template<TaskData> = createTemplate {

            nextBlock { handler ->
                handler.applyToResult { row -> "[ $row ]" }
                val status = when (executionStatus) {
                    ExecutionStatus.Complete -> executionStatus.name.colorize(Colour.GREEN)
                    ExecutionStatus.Active -> executionStatus.name.colorize(Colour.BRIGHT_WHITE)
                    ExecutionStatus.Failing, ExecutionStatus.Faulty -> executionStatus.name.colorize(Colour.RED)
                }
                "$taskHeader | Status: ".colorize(Colour.BLUE) + status + "".colorize(Colour.BLUE)
            }
        }
        val Footer: Template<TaskData> = createTemplate {
            nextBlock { handler ->
                handler.applyToResult { row -> "[ $row ]" }
                taskFooter.colorize(Colour.BLUE)
            }
        }
    }
}