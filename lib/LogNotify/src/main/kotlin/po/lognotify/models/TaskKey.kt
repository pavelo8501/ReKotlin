package po.lognotify.models

class TaskKey(
    val taskName: String,
    val nestingLevel: Int,

) {
   internal val taskId : Long = System.currentTimeMillis()

    fun asString(): String{
        return "${taskName}|$nestingLevel|$taskId"
    }
}