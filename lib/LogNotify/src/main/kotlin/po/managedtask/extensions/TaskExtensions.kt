package po.managedtask.extensions

import kotlinx.coroutines.runBlocking
import po.managedtask.classes.ManagedResult
import po.managedtask.classes.task.TaskHandler
import po.managedtask.exceptions.DefaultException
import po.managedtask.exceptions.enums.DefaultType
import po.managedtask.helpers.StaticsHelperProvider
import po.managedtask.interfaces.TasksManaged
import po.managedtask.models.TaskKey
import kotlin.coroutines.CoroutineContext


suspend  fun <R> TasksManaged.startTask(
    taskName: String,
    coroutine: CoroutineContext,
    block: suspend TaskHandler.(StaticsHelperProvider)-> R,
): ManagedResult<R> {
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName, coroutine)
    newTask.initializeComponents()
    val executionResult = newTask.runTask(block)
    return executionResult
}


fun <R> TasksManaged.startTaskAsync(
    taskName: String,
    block: suspend TaskHandler.(StaticsHelperProvider)-> R,
): R {
    val newTask = TasksManaged.createHierarchyRoot<R>(taskName)
    val result =  runBlocking {
        newTask.initializeComponents()
        val result = newTask.runTaskInDefaultContext(block)
        result.await()
    }
    return result.value.getOrThrowDefault("Exception was unhandled result impossible")
}


suspend inline fun <R>  TaskHandler.withTask(
    key: TaskKey,
    block : suspend TaskHandler.(StaticsHelperProvider)-> R
): ManagedResult<R>{
    val task = TasksManaged.taskFromRegistry<R>(key).getOrThrowDefault("Task registry failure no key ${key.asString()} found")
   // block.invoke(task, task.helper)
    val casted =  task.taskResult.safeCast<ManagedResult<R>>().getOrThrowDefault("Task registry failure no key ${key.asString()} found")
    return casted
}


suspend  fun <R>  TasksManaged.subTask(
    taskName: String,
    block: suspend TaskHandler.(StaticsHelperProvider)-> R
): ManagedResult<R>
{

    TasksManaged.attachToHierarchy<R>(taskName)?.let {
       val task = it
       return  task.runTask(block)
    }?:run {
       throw DefaultException("Unable to create sub task $taskName", DefaultType.DEFAULT)
    }
}


