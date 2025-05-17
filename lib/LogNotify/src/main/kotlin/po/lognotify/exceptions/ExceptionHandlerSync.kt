package po.lognotify.exceptions

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.models.Notification
import po.lognotify.classes.notification.sealed.ProviderTask
import po.lognotify.classes.task.TaskBaseSync
import po.lognotify.enums.SeverityLevel
import po.lognotify.exceptions.ExceptionHandler.HandlerResult
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import kotlin.collections.forEach
import kotlin.collections.set


class HandledResult<R: Any?>(
    val task:  TaskBaseSync<R>,
    val value:R? = null,
){

    var exception: ManagedException? = null
    var success: Boolean = true

    fun provideException(th: ManagedException):HandledResult<R>{
        exception    = th
        success = false
        return  this
    }
}

class ExceptionHandlerSync<R: Any?>(
    internal val task : TaskBaseSync<R>,
) {


    private fun notifyHandlerSet(handler : HandlerType){
        val message = "$handler handle set"
        val notification = Notification(
            ProviderTask(task),
            EventType.HANDLER_REGISTERED,
            SeverityLevel.INFO,
            message)
        task.notifier.submitNotification(notification)
    }
    private fun notifyHandled(managedEx : ManagedException){
        val message =  "Exception Handled. Exception Message : ${managedEx.message}"
        val severity = SeverityLevel.WARNING

        val notification = Notification(
            ProviderTask(task),
            EventType.EXCEPTION_UNHANDLED,
            severity,
            message)

        task.notifier.submitNotification(notification)
    }
    private fun notifyUnhandled(managedEx : ManagedException){
        val severity = SeverityLevel.EXCEPTION

        var message = """ 
          Unhandled in Task:${task.taskId.name}| Module: ${task.key.moduleName}| Nesting: ${task.key.nestingLevel}
          Message: ${managedEx.message}
      """.trimIndent()

        if(managedEx.snapshot != null){
            val snapshotStr = managedEx.snapshot!!.map { "${it.key} = ${it.value}" }.joinToString(";")
            message = """  
             Unhandled in Task:${task.key.taskName}| Module: ${task.key.moduleName}| Nesting: ${task.key.nestingLevel}
             Message: ${managedEx.message}
             Snapshot: $snapshotStr
         """.trimIndent()
        }

        val notification = Notification(
            ProviderTask(task),
            EventType.EXCEPTION_UNHANDLED,
            severity,
            message)

        task.notifier.submitNotification(notification)
    }

    val registeredHandlers : MutableMap<HandlerType, ( exception: ManagedException)->R> = mutableMapOf()

    fun provideHandlerFn(handlers : Set<HandlerType>, handlerFn:  (exception: ManagedException)->R){
        handlers.forEach {
            registeredHandlers[it] = handlerFn
            notifyHandlerSet(it)
        }
    }

    suspend fun isHandlerPresent(handler: HandlerType): Boolean{
        return  registeredHandlers[handler]?.let { true }?:false
    }

    fun handleManaged(managedEx: ManagedException):  HandledResult<R> {
        val handlerFn = registeredHandlers[managedEx.handler]
        val handlingResult = handlerFn?.let {
            notifyHandled(managedEx)
            val result =  handlerFn.invoke(managedEx)
            HandledResult(task, result)
        } ?:run {
            notifyUnhandled(managedEx)
            HandledResult(task, null).provideException(managedEx)
        }
        return handlingResult
    }
}

fun <R> ManagedException.toHandledResult(task : TaskBaseSync<R>): HandledResult<R>{
   return HandledResult(task, null).provideException(this)
}