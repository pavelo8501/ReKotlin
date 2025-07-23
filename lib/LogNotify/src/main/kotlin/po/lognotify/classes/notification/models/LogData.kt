package po.lognotify.classes.notification.models

import po.lognotify.common.LogInstance
import po.lognotify.tasks.models.TaskConfig
import po.lognotify.enums.SeverityLevel
import po.lognotify.tasks.ExecutionStatus
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.data.printable.PrintableGroup
import po.misc.data.styles.colorize
import po.misc.data.styles.Colour
import po.misc.data.templates.matchTemplate
import po.misc.data.templates.templateRule

import po.misc.time.ExecutionTimeStamp


class LifecycleDataGroup(
    val logData: TaskData
): PrintableGroup<TaskData, LogEvent>(logData, TaskData.Header, LogEvent.Message)


class LogEvent(
    val logInstance: LogInstance<*>,
    val message: String,
    val severity: SeverityLevel
): PrintableBase<LogEvent>(Message){

    override val self: LogEvent = this

    companion object: PrintableCompanion<LogEvent>({LogEvent::class}) {

        val Message = createTemplate{
            next {
                matchTemplate(
                    templateRule(message) { severity == SeverityLevel.INFO },
                    templateRule(message) { severity == SeverityLevel.LOG },
                    templateRule(message.colorize(Colour.YELLOW)) { severity == SeverityLevel.WARNING },
                    templateRule(message.colorize(Colour.RED)) { severity == SeverityLevel.EXCEPTION }
                )
            }
        }
    }
}

data class TaskData(
    val executionStatus: ExecutionStatus,
    val taskHeader: String,
    val taskFooter: String,
    val config: TaskConfig,
    val timeStamp : ExecutionTimeStamp,
    val message: String = "",
    val severity: SeverityLevel = SeverityLevel.INFO
): PrintableBase<TaskData>(Message) {
    override val self: TaskData = this
    companion object : PrintableCompanion<TaskData>({ TaskData::class }) {

        val Header = createTemplate {
            next { "$taskHeader Status[" }
            next {
                val status = when (executionStatus) {
                    ExecutionStatus.Complete -> executionStatus.name.colorize(Colour.GREEN)
                    ExecutionStatus.Active -> executionStatus.name.colorize(Colour.BRIGHT_WHITE)
                    ExecutionStatus.Failing, ExecutionStatus.Faulty -> executionStatus.name.colorize(Colour.RED)
                }
                "$status]"
            }
        }
        val Footer = createTemplate{
            next { taskFooter }
        }

        val Message = createTemplate{
            next {
                when(severity){
                    SeverityLevel.INFO->message.colorize(Colour.GREEN)
                    SeverityLevel.LOG->message.colorize(Colour.BRIGHT_WHITE)
                    SeverityLevel.WARNING->message.colorize(Colour.YELLOW)
                    SeverityLevel.EXCEPTION->message.colorize(Colour.RED)
                    SeverityLevel.SYS_INFO,SeverityLevel.DEBUG->message.colorize(Colour.MAGENTA)
                }
            }
        }
    }
}