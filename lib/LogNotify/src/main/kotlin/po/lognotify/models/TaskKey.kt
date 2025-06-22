package po.lognotify.models

import po.misc.collections.CompositeKey

class TaskKey(
    val taskName: String,
    val nestingLevel: Int,
    val moduleName: String
): Comparable<TaskKey> {
   internal val taskId : Int =  System.currentTimeMillis().toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TaskKey) return false
        return taskId == other.taskId
    }
    override fun hashCode(): Int {
        return taskId
    }
    override fun compareTo(other: TaskKey): Int {
        return compareValuesBy(this, other,
            { it.nestingLevel },
            { it.taskId }
        )
    }

    override fun toString(): String = "TaskKey(${taskId}, $moduleName)"

}