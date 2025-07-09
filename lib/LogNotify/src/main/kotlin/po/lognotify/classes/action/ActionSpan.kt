package po.lognotify.classes.action

import po.lognotify.TaskProcessor
import po.lognotify.tasks.TaskHandler
import po.lognotify.models.TaskKey
import po.misc.exceptions.ManagedException
import po.misc.interfaces.ClassIdentity
import po.misc.reflection.classes.ClassInfo
import po.misc.time.ExecutionTimeStamp

class ActionSpan<T>(
    val actionName: String,
    val taskHandler: TaskHandler<*>,
    val ctx: T,
): TaskProcessor where T: InlineAction {

    enum class Status{
        Active,
        Complete,
        Failed
    }

    val inTask: TaskKey get()= taskHandler.task.key

    var status : Status = Status.Active
        private set

    val shortName: String get() = "ActionSpan(Method: $actionName; CTX: ${ctx.contextName})"
    override val identity: ClassIdentity = ClassIdentity.create(shortName, ctx.contextName)

    val executionTime: ExecutionTimeStamp = ExecutionTimeStamp(shortName, "0")
    var managed: ManagedException?  = null
    var failedClassInfo: ClassInfo<*>? = null
    private var onExceptionCallback: ((ActionSpan<T>)-> Unit)? = null

    fun onException(callback:(ActionSpan<T>)-> Unit){
        onExceptionCallback = callback
    }

    fun complete(){
        status = Status.Complete
        executionTime.stopTimer()
    }

    fun complete(exception: ManagedException, classInfo: ClassInfo<*>){
        status = Status.Failed
        failedClassInfo = classInfo
        executionTime.stopTimer()
        managed = exception
        onExceptionCallback?.invoke(this)?:run {
            throw exception
        }
    }

    override fun toString(): String {
       return "$shortName Within $inTask"
    }
}