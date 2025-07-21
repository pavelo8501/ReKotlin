package po.lognotify.action


import kotlinx.coroutines.withContext
import po.lognotify.TasksManaged
import po.lognotify.action.models.ActionData
import po.lognotify.anotations.LogOnFault
import po.lognotify.common.LogInstance
import po.lognotify.tasks.TaskHandler
import po.lognotify.models.TaskKey
import po.lognotify.tasks.ExecutionStatus
import po.lognotify.tasks.TaskBase
import po.misc.context.CTX
import po.misc.context.subIdentity
import po.misc.data.printable.knowntypes.PropertyData
import po.misc.exceptions.ManagedException
import po.misc.reflection.classes.ClassInfo
import po.misc.reflection.properties.takePropertySnapshot
import po.misc.time.ExecutionTimeStamp
import kotlin.reflect.KType

class ActionSpan<T, R: Any?>(
    val actionName: String,
    val taskHandler: TaskHandler<*>,
    override val receiver: T,
): LogInstance<T>  where T: CTX {

    override val identity = subIdentity(this, receiver)

    override val contextName: String
        get() = "ActionSpan"

    val inTask: TaskKey get()= taskHandler.task.key
    val taskBase : TaskBase<*, *> get() = taskHandler.task

    var actionSpanStatus : ExecutionStatus = ExecutionStatus.Active
        private set

    val shortName: String get() = "ActionSpan[${actionName}] in Context[${receiver.contextName}]"

    val executionTime: ExecutionTimeStamp = ExecutionTimeStamp(shortName, "0")
    var managed: ManagedException?  = null
    var failedClassInfo: ClassInfo<R>? = null
    private var onExceptionCallback: ((ActionSpan<T, R>)-> Unit)? = null

    internal var resultType: KType? = null

    fun onException(callback:(ActionSpan<T, R>)-> Unit){
        onExceptionCallback = callback
        changeStatus(ExecutionStatus.Faulty)
    }

    override fun changeStatus(status:ExecutionStatus){
        actionSpanStatus = status
    }

    fun createData(): ActionData {
        val data = ActionData(
            actionSpan = this,
            actionName = actionName,
            status = actionSpanStatus,
            propertySnapshot = createPropertySnapshot(),
        )
        return data
    }


    fun createPropertySnapshot(): List<PropertyData>{
       return takePropertySnapshot<T, LogOnFault>(receiver)
    }

    fun complete(){
        actionSpanStatus = ExecutionStatus.Complete
        executionTime.stopTimer()
    }

    fun complete(exception: ManagedException, classInfo: ClassInfo<R>){
        actionSpanStatus = ExecutionStatus.Faulty
        failedClassInfo = classInfo
        executionTime.stopTimer()
        managed = exception
        onExceptionCallback?.invoke(this)?:run {
            throw exception
        }
    }

    fun checkIfCanBeSkipped(classInfo: ClassInfo<R>?): Boolean{
        if (classInfo == null) {
            return false
        }
        if (classInfo.acceptsNull) {
            return true
        } else {
            return false
        }
    }

    override fun toString(): String {
        return createData().formattedString
    }
}