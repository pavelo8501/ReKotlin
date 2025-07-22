package po.lognotify.classes.notification.models


import po.lognotify.classes.notification.models.ActionData
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

        val Header = createTemplate{
            next { "$taskHeader Status[" }
            with({it.executionStatus}){
                next{
                    matchTemplate(
                        templateRule(name.colorize(Colour.GREEN))
                        { this ==   ExecutionStatus.Complete},
                        templateRule(name.colorize(Colour.BRIGHT_WHITE))
                        { this ==   ExecutionStatus.Active},
                        templateRule(name.colorize(Colour.RED))
                        { this ==   ExecutionStatus.Failing},
                        templateRule(name.colorize(Colour.RED))
                        { this ==   ExecutionStatus.Faulty})
                }
                next{ "]" }
            }
        }
        val Footer = createTemplate{
            next { taskFooter }
        }

        val Message =createTemplate{
            next {
                matchTemplate(
                    templateRule(message.colorize(Colour.GREEN))
                    { severity == SeverityLevel.INFO},
                    templateRule(message.colorize(Colour.YELLOW))
                    { severity ==   SeverityLevel.WARNING},
                    templateRule(message.colorize(Colour.RED))
                    { severity ==   SeverityLevel.EXCEPTION})
            }
        }
    }
}