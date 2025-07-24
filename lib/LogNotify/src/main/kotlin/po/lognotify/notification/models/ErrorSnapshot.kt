package po.lognotify.notification.models

import po.lognotify.tasks.ExecutionStatus
import po.lognotify.tasks.TaskBase
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.data.styles.SpecialChars
import po.misc.functions.builders.initializeBuilder

data class ErrorSnapshot(
    val taskHeader: String,
    val taskStatus: ExecutionStatus
){
    var actionRecords: List<ActionData> = emptyList()
}

