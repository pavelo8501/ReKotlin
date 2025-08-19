package po.lognotify.models

import po.misc.collections.CompositeKey
import java.util.UUID

class TaskKey(
    val taskName: String,
    val nestingLevel: Int,
    val moduleName: String,
) : Comparable<TaskKey> {

    val taskId: UUID = UUID.randomUUID()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TaskKey) return false
        return taskId == other.taskId
    }

    override fun hashCode(): Int = taskId.hashCode()

    override fun compareTo(other: TaskKey): Int =
        compareValuesBy(
            this, other,
            { it.nestingLevel },
            { it.taskId.toString() }
        )

    override fun toString(): String {
        val nesting =
            if (nestingLevel == 0) {
                "R Task: "
            } else {
                "$nestingLevel Task: "
            }
        return "$nesting $taskName | $moduleName"
    }
}

class SpanKey(
    val actionSpanName: String,
    val nestingLevel: Int,
    val taskName: String,
    val taskNestingLevel: Int,
) : Comparable<SpanKey> {
    internal val spanId: Int = System.currentTimeMillis().toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SpanKey) return false
        return spanId == other.spanId
    }

    override fun hashCode(): Int = spanId

    override fun compareTo(other: SpanKey): Int =
        compareValuesBy(
            this,
            other,
            { it.nestingLevel },
            { it.spanId },
        )

    override fun toString(): String = "[AS] $actionSpanName"
}
