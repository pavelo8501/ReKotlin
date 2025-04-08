package po.lognotify.models

class TaskKey(
    val taskName: String,
    val nestingLevel: Int,
    val moduleName: String?= null
) {
   internal val taskId : Long = System.currentTimeMillis()

    fun asString(): String{
        if(moduleName != null){
            return "Task ${taskName} In $moduleName|NestingLevel $nestingLevel"
        }
        return "Task ${taskName} |NestingLevel $nestingLevel"
    }
}