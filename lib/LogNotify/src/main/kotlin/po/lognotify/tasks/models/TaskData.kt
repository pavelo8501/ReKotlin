package po.lognotify.tasks.models

import po.lognotify.action.models.ActionData
import po.lognotify.tasks.TaskBase
import po.misc.context.CTX
import po.misc.data.printable.PrintableTemplate
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.data.styles.SpecialChars


data class TaskData(

    val taskBase: TaskBase<*, *>,
): PrintableBase<TaskData>(Default) {

    override val self: TaskData = this
    override val producer: CTX get() = taskBase

    var actionSpanRecords: List<ActionData> = emptyList()

    companion object : PrintableCompanion<TaskData>({ TaskData::class }) {
//        val TaskInfo = PrintableTemplate<TaskData>(){
//            "${this.taskBase.toString()}"
//        }
        val Default = createTemplate(){
            next { "$taskBase Result: ${taskBase.taskResult} ${SpecialChars.NewLine}" }
            next { actionSpanRecords.joinToString(separator = SpecialChars.NewLine.char) { it.formattedString } }
        }
    }
}