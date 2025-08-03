package po.exposify.dto.helpers

import po.lognotify.TasksManaged
import po.misc.data.processors.SeverityLevel
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize

internal fun <T : Any?> T.notifyIfNull(
    message: String,
    context: Any,
    severity: SeverityLevel = SeverityLevel.WARNING,
): T {
    if (this == null) {
        when (context) {
            is TasksManaged -> {
                with(context) {
                    notify(message, severity)
                }
            }
            else -> println(message.colorize(Colour.Yellow))
        }
    }
    return this
}


internal fun  TasksManaged.warning(
     message: String,
): Unit = notify(message, SeverityLevel.WARNING)


