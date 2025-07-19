package po.lognotify.action


import po.lognotify.TasksManaged
import po.lognotify.action.models.ActionData
import po.lognotify.anotations.LogOnFault
import po.lognotify.tasks.TaskHandler
import po.lognotify.models.TaskKey
import po.lognotify.tasks.TaskBase
import po.misc.data.printable.knowntypes.PropertyData
import po.misc.exceptions.ManagedException
import po.misc.interfaces.ClassIdentity
import po.misc.context.IdentifiableClass
import po.misc.reflection.classes.ClassInfo
import po.misc.reflection.properties.takePropertySnapshot
import po.misc.time.ExecutionTimeStamp
import kotlin.reflect.KType

class ActionSpan<T, R: Any?>(
    val actionName: String,
    val taskHandler: TaskHandler<*>,
    val receiver: T,
): IdentifiableClass  where T: TasksManaged {

    enum class Status{
        Active,
        Complete,
        Failed
    }

    override val identity: ClassIdentity = ClassIdentity.create("ActionSpan", receiver.contextName)

    override val contextName: String
        get() = "ActionSpan"

    val inTask: TaskKey get()= taskHandler.task.key
    val taskBase : TaskBase<*, *> get() = taskHandler.task

    var status : Status = Status.Active
        private set

    val shortName: String get() = "ActionSpan[${actionName}] in Context[${receiver.contextName}]"

    val executionTime: ExecutionTimeStamp = ExecutionTimeStamp(shortName, "0")
    var managed: ManagedException?  = null
    var failedClassInfo: ClassInfo<R>? = null
    private var onExceptionCallback: ((ActionSpan<T, R>)-> Unit)? = null

    internal var resultType: KType? = null

    fun onException(callback:(ActionSpan<T, R>)-> Unit){
        onExceptionCallback = callback
        setStatus(Status.Failed)
    }
    fun setStatus(newStatus : Status){
        status = newStatus
    }

    fun createData(): ActionData {
        val data = ActionData(
            actionSpan = this,
            actionName = actionName,
            status = status,
            propertySnapshot = createPropertySnapshot(),
        )
        return data
    }


    fun createPropertySnapshot(): List<PropertyData>{
       return takePropertySnapshot<T, LogOnFault>(receiver)
    }

    fun complete(){
        status = Status.Complete
        executionTime.stopTimer()
    }

    fun complete(exception: ManagedException, classInfo: ClassInfo<R>){
        status = Status.Failed
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