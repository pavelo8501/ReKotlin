package po.lognotify.classes.notification.sealed

import po.lognotify.classes.task.TaskIdentification

data class ProviderTask(val task: TaskIdentification) :InfoProvider(task)
data class ProviderThrower(val task: TaskIdentification) :InfoProvider(task)
data class ProviderHandler(val task: TaskIdentification) :InfoProvider(task)

sealed class InfoProvider(
    task: TaskIdentification
){

    val coroutineContext = task.coroutineContext


}