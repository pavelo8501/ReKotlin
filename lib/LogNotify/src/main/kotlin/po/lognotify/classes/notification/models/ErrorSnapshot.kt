package po.lognotify.classes.notification.models

import po.lognotify.tasks.TaskBase
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.data.styles.SpecialChars
import po.misc.functions.builders.initializeBuilder

data class ErrorSnapshot(
    val taskBase: TaskBase<*, *>,
): PrintableBase<ErrorSnapshot>(Default) {
    override val self: ErrorSnapshot = this
    var actionSpanRecords: List<ActionData> = emptyList()

    companion object : PrintableCompanion<ErrorSnapshot>({ ErrorSnapshot::class }) {
        val errorSnapshotBuilder = initializeBuilder<TaskBase<*, *>, ErrorSnapshot>  { task->
            ErrorSnapshot(task)
        }
        val Default = createTemplate() {
            next { "$taskBase Result: ${taskBase.taskResult} ${SpecialChars.NewLine}" }
            next { actionSpanRecords.joinToString(separator = SpecialChars.NewLine.char) { it.formattedString } }
        }
    }
}