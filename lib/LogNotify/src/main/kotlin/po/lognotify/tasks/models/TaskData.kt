package po.lognotify.tasks.models

import po.lognotify.action.models.ActionData
import po.lognotify.tasks.TaskBase
import po.misc.data.printable.PrintableTemplate
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.data.styles.SpecialChars
import po.misc.context.IdentifiableClass


data class TaskData(
    val taskBase: TaskBase<*, *>,
): PrintableBase<TaskData>(Default) {

    override val self: TaskData = this
    override val producer: IdentifiableClass get() = taskBase

    var actionSpanRecords: List<ActionData> = emptyList()

    companion object : PrintableCompanion<TaskData>({ TaskData::class }) {

        val TaskInfo = PrintableTemplate<TaskData>("TaskInfo"){
            "${this.taskBase.toString()}"
        }

        val Default = PrintableTemplate<TaskData>("Default") {
            "$taskBase Result: ${taskBase.taskResult} ${SpecialChars.NewLine} " +
            actionSpanRecords.joinToString(separator = SpecialChars.NewLine.char) {
                it.formattedString
            }
        }
    }
}