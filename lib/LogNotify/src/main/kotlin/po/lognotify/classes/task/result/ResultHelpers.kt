package po.lognotify.classes.task.result

import po.lognotify.classes.task.TaskBase
import po.lognotify.classes.task.interfaces.ResultantTask
import po.misc.exceptions.ManagedException



//fun <R> R.toTaskResult(task : ResultantTask<R>): TaskResult<R>{
//    return TaskResult<R>(task).provideResult(this)
//}

fun<R: Any?> TaskBase<R>.toTaskResult(result:R): TaskResult<R>{
    return TaskResult(this, result = result).onResultProvided()
}

fun<R: Any?> TaskBase<R>.toTaskResult(throwable: Throwable): TaskResult<R>{
    return TaskResult(this, throwable = throwable)
}


//fun <R> ManagedException.toTaskResult(task : ResultantTask<R>): TaskResult<R>{
//    return TaskResult<R>(task, this).provideThrowable(this)
//}

inline fun <R: Any?> TaskResult<R>.onFailureCause(block: (Throwable) -> Unit): TaskResult<R> {
    if (!isSuccess && throwable != null) block.invoke(throwable!!)
    return this
}

fun <R: Any?> TaskResult<R>.toKotlinResult(): Result<R?> =
    runCatching {  this.resultOrException() }


fun <R: Any?> TaskResult<R>.resultOrNull():R?{
   return this.result

}