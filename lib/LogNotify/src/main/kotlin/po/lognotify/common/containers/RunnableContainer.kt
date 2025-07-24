package po.lognotify.common.containers

import po.lognotify.TasksManaged
import po.lognotify.action.ActionSpan
import po.lognotify.common.LNInstance
import po.lognotify.notification.LoggerContract
import po.lognotify.notification.LoggerDataProcessor
import po.lognotify.execution.ControlledExecution
import po.lognotify.tasks.ExecutionStatus
import po.lognotify.tasks.RootTask
import po.lognotify.tasks.TaskBase
import po.lognotify.tasks.TaskHandler
import po.lognotify.tasks.models.TaskConfig
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.exceptions.ExceptionPayload
import po.misc.functions.containers.DeferredContainer
import po.misc.reflection.classes.ClassInfo


sealed class RunnableContainer<T: CTX, R: Any?>(
   internal val source : LNInstance<T>,
   val notifier: LoggerDataProcessor
): ControlledExecution, LoggerContract by notifier{

    override val identity: CTXIdentity<out CTX>
        get() = source.identity


    val receiver:T get()  = source.receiver

    override val contextName: String get() = receiver.contextName

    val exPayload: ExceptionPayload = ExceptionPayload(this)
    var verboseMode: Boolean = false

    abstract val classInfoProvider: DeferredContainer<ClassInfo<R>>
    val attempts: Int get() {
      return  when(this){
            is TaskContainer<*, *> -> {
                this.sourceTask.config.attempts
            }
            is  ActionContainer->{
                1
            }
        }
    }
    private var resultBacking : R? = null

    abstract val effectiveTask: TaskBase<*, *>
    abstract val effectiveActionSpan: ActionSpan<* , *>?
    abstract val taskHandler: TaskHandler<*>
    abstract val taskConfig: TaskConfig

    val isRoot: Boolean get() = effectiveTask is RootTask
    var resultNullable: Boolean? = null
    var classInfo: ClassInfo<*>? = null

    fun notifyResult(){
        when(this){
            is ActionContainer<*, *> -> {
                actionSpan.shortName
            }

            is TaskContainer<*, *> -> {

            }
        }
    }
    fun onResultResolved(result:R){
        resultBacking = result
        notifyResult()
    }

    fun notifySourceIsFailing(){
        source.changeStatus(ExecutionStatus.Failing)
    }

    fun <R2> estimateReturnNullability(classInfo : ClassInfo<R2>): Boolean{
       if(classInfo.acceptsNull){
           resultNullable = true
       }else{
           resultNullable = false
       }
        this.classInfo = classInfo
        return resultNullable?:false
    }
}


class TaskContainer<T: CTX, R: Any?>(
    val sourceTask: TaskBase<T, R>,
): RunnableContainer<T, R>(sourceTask, sourceTask.dataProcessor) {



    override val effectiveTask: TaskBase<T, R> get() = sourceTask
    override val taskHandler: TaskHandler<R> get() = sourceTask.handler
    override val taskConfig: TaskConfig = sourceTask.config
    override val effectiveActionSpan: ActionSpan<*, *>?
        get() = sourceTask.activeActionSpan()

    override val classInfoProvider: DeferredContainer<ClassInfo<R>> = DeferredContainer(sourceTask)

    companion object {
        fun <T: TasksManaged, R: Any?> create(
            task: TaskBase<T, R>
        ): TaskContainer<T,R>{
            return TaskContainer(task)
        }
    }
}

class ActionContainer<T: TasksManaged, R: Any?>(
    val actionSpan: ActionSpan<T, R>
): RunnableContainer<T, R>(actionSpan, actionSpan.task.dataProcessor) {


    override val effectiveTask: TaskBase<*, *> get() = actionSpan.task
    override val taskHandler: TaskHandler<*> get() = actionSpan.task.handler
    override val taskConfig: TaskConfig = taskHandler.taskConfig
    override val effectiveActionSpan: ActionSpan<*, *> get() = actionSpan

    override val classInfoProvider: DeferredContainer<ClassInfo<R>> = DeferredContainer(actionSpan)

    companion object {
        fun <T: TasksManaged, R: Any?> create(
            actionSpan: ActionSpan<T, R>,
        ): ActionContainer<T, R>{
            return ActionContainer(actionSpan)
        }
    }
}

