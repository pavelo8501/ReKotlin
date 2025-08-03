package po.lognotify.action

import po.lognotify.anotations.LogOnFault
import po.lognotify.common.LNInstance
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.models.SpanKey
import po.lognotify.models.TaskKey
import po.lognotify.notification.models.ActionData
import po.lognotify.tasks.ExecutionStatus
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.TaskBase
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asSubIdentity
import po.misc.data.printable.knowntypes.PropertyData
import po.misc.exceptions.ManagedException
import po.misc.reflection.classes.ClassInfo
import po.misc.reflection.properties.takePropertySnapshot
import po.misc.time.ExecutionTimeStamp
import kotlin.reflect.KType

class ActionSpan<T : CTX, R : Any?>(
    val actionName: String,
    val spanKey: SpanKey,
    override val receiver: T,
    internal val task: TaskBase<*, *>,
) : LNInstance<T> {
    override val identity: CTXIdentity<ActionSpan<T, R>> = asSubIdentity(this, receiver)

    override val contextName: String
        get() = "ActionSpan"

    override val config: TaskConfig get() = task.config
    val inTask: TaskKey get() = task.key

    override val nestingLevel: Int get() = spanKey.nestingLevel
    override val rootTask: RootTask<*, *> get() = task.rootTask

    override val header: String get() = "AS $actionName in Context[${receiver.contextName}]"

    override var executionStatus: ExecutionStatus = ExecutionStatus.Active
        private set

    val shortName: String get() = "ActionSpan[$actionName] in Context[${receiver.contextName}]"

    val executionTime: ExecutionTimeStamp = ExecutionTimeStamp(shortName, "0")
    var managed: ManagedException? = null

    var failedClassInfo: ClassInfo<R>? = null

    private var onExceptionCallback: ((ActionSpan<T, R>) -> Unit)? = null

    var resultType: KType? = null
        private set

    fun onException(callback: (ActionSpan<T, R>) -> Unit) {
        onExceptionCallback = callback
        changeStatus(ExecutionStatus.Faulty)
    }

    fun setResultType(kType: KType) {
        resultType = kType
    }

    override fun changeStatus(status: ExecutionStatus) {
        executionStatus = status
    }

    fun createData(): ActionData {
        val data =
            ActionData(
                actionName = actionName,
                actionStatus = executionStatus,
                propertySnapshot = createPropertySnapshot(),
            )
        return data
    }

    fun createPropertySnapshot(): List<PropertyData> = takePropertySnapshot<T, LogOnFault>(receiver)

    private var propertySnapshotBacking: List<PropertyData> = listOf()
    val propertySnapshot: List<PropertyData> get() = propertySnapshotBacking

    fun addPropertySnapshot(snapshot: List<PropertyData>): List<PropertyData> {
        propertySnapshotBacking = snapshot
        return snapshot
    }

    override fun complete(): LNInstance<*> {
        executionStatus = ExecutionStatus.Complete
        executionTime.stopTimer()
        return this
    }

    override fun complete(exception: ManagedException): Nothing {
        executionStatus = ExecutionStatus.Faulty
        executionTime.stopTimer()
        managed = exception
        throw exception
    }

    fun checkIfCanBeSkipped(classInfo: ClassInfo<R>?): Boolean {
        if (classInfo == null) {
            return false
        }
        if (classInfo.acceptsNull) {
            return true
        } else {
            return false
        }
    }

    override fun toString(): String = header
}
