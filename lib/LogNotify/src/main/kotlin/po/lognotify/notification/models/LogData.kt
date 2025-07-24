package po.lognotify.notification.models

import po.lognotify.tasks.models.TaskConfig
import po.lognotify.enums.SeverityLevel
import po.lognotify.tasks.ExecutionStatus
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.data.printable.PrintableGroup
import po.misc.data.printable.Template2
import po.misc.data.styles.colorize
import po.misc.data.styles.Colour
import po.misc.data.text.applyIfNotEmpty
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

): PrintableBase<LogEvent>(Message){
    override val self: LogEvent = this

    var exceptionRecord:ExceptionRecord? = null

    companion object: PrintableCompanion<LogEvent>({LogEvent::class}) {

        val Message : Template2<LogEvent> = createTemplate2 {
            nextBlock {
               val message = when(severity){
                    SeverityLevel.INFO, SeverityLevel.LOG -> message.colorize(Colour.GREEN)
                    SeverityLevel.WARNING -> message.colorize(Colour.YELLOW)
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
    val stackTraceElement: StackTraceElement?

): PrintableBase<ExceptionRecord>(Default) {

    override val self: ExceptionRecord = this

    companion object : PrintableCompanion<ExceptionRecord>({ ExceptionRecord::class }) {

        val Default: Template2<ExceptionRecord> = createTemplate2 {
            nextBlock{
                message.colorize(Colour.RED)
            }
            nextBlock{
                "First registered: $firstRegisteredInTask"
            }
            nextBlock{
                stackTraceElement?.toString()?:""
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
) : PrintableBase<TaskData>(Header) {
    override val self: TaskData = this

    val events:TaskEvents = TaskEvents(this)
    val arbitraryData: MutableList<PrintableBase<*>> = mutableListOf()


    companion object : PrintableCompanion<TaskData>({ TaskData::class }) {
        val Header: Template2<TaskData> = createTemplate2 {
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
        val Footer: Template2<TaskData> = createTemplate2 {
            nextBlock { handler ->
                handler.applyToResult { row -> "[ $row ]" }
                taskFooter.colorize(Colour.BLUE)
            }
        }
    }
}