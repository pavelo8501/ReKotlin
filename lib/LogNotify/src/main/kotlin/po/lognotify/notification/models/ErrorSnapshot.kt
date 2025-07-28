package po.lognotify.notification.models

import po.lognotify.tasks.ExecutionStatus


data class ErrorSnapshot(
    val taskHeader: String,
    val taskStatus: ExecutionStatus
) {
    var actionRecords: List<ActionData> = emptyList()
}

