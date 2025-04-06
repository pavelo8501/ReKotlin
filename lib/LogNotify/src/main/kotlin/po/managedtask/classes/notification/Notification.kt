package po.managedtask.classes.notification

import po.managedtask.enums.SeverityLevel

import java.time.LocalDateTime


enum class InfoProvider{
    EX_THROWER,
    EX_HANDLER
}

enum class NotifyTask{
    HANDLER_SET,
    EXCEPTION_HANDLED,
    EXCEPTION_UNHANDLED,
    THROWN
}

data class Notification(
    val task: String,
    val taskNestingLevel: Int,
    val action : NotifyTask,
    val severity: SeverityLevel,
    val message: String,
    val provider: InfoProvider,
    val original : String,
    val timestamp: LocalDateTime
): JasonStringSerializable