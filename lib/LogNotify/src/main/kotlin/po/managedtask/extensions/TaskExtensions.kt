package po.managedtask.extensions

import po.managedtask.classes.ManagedTask
import po.managedtask.classes.TaskResult
import po.managedtask.exceptions.DefaultException
import po.managedtask.exceptions.ExceptionThrower
import po.managedtask.helpers.StaticsHelper
import po.managedtask.helpers.StaticsHelperProvider
import po.managedtask.interfaces.TasksManaged
import kotlin.coroutines.CoroutineContext


suspend  fun <T: TasksManaged, R: Any> T.startTask(
    taskName: String,
    coroutine: CoroutineContext? = null,
    block: suspend ManagedTask<Any?>.(StaticsHelperProvider)-> R?,
): TaskResult<R?> {

    val newTask = ManagedTask<R>(StaticsHelper(taskName), ExceptionThrower(), null, block)
    if(coroutine != null){
        newTask.withCoroutine(coroutine)
    }
    val executionResult = newTask.runTask()
    return executionResult
}

suspend  fun <T: ManagedTask<*>, CR: Any> T.startTask(
    taskName: String,
    block: suspend ManagedTask<Any?>.(StaticsHelperProvider)-> CR?,
): TaskResult<CR?> {
    val parent = this
    val child = ManagedTask<Any?>(StaticsHelper("$taskName:${parent.taskName}"), ExceptionThrower(), parent, block)
    val castedChild = child.safeCast<ManagedTask<CR?>>().getOrThrow(DefaultException("Cast failed"))
    return  castedChild.runTask()
}


