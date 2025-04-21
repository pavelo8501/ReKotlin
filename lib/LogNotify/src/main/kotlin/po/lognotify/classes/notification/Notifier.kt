package po.lognotify.classes.notification

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.models.Notification
import po.lognotify.classes.notification.sealed.ProviderTask
import po.lognotify.classes.task.ResultantTask
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.enums.ColourEnum
import po.lognotify.enums.SeverityLevel
import po.lognotify.helpers.StaticHelper

interface NotificationProvider{

    suspend fun start()

    fun echo(message: String)
    suspend fun info(message: String)
    suspend fun error(ex: Throwable, optMessage: String)
    suspend fun warn(message: String)
}

class Notifier(
    private val task: TaskSealedBase<*>,
) : NotificationProvider, StaticHelper{
    private val _notification = MutableSharedFlow<Notification>(extraBufferCapacity = 64)
    val notification: SharedFlow<Notification> = _notification.asSharedFlow()

    private suspend fun emit(notification : Notification){
        _notification.emit(notification)
    }


    fun toConsole(notification : Notification){
        when(notification.eventType){
            EventType.START -> {
              val header = notification.getTaskHeader()
              println(header)
            }
            EventType.STOP -> {
                val header = notification.getTaskFooter()
                println(header)
            }
            else ->{
                val formattedString = notification.getMessagePrefixed()
                println(formattedString)
            }
        }
    }

    internal suspend fun createTaskNotification(task : ResultantTask, message: String, type : EventType, severity: SeverityLevel){
        val notification = Notification(
            task,
            type,
            severity,
            message,
            ProviderTask(task.taskName)
        )
        toConsole(notification)
        emit(notification)
    }
    suspend fun subscribeToHandlerUpdates(){
        withContext(task.context) {
            task.taskRunner.exceptionHandler.subscribeHandlerUpdates(){
                toConsole(it)
                emit(it)
            }
        }
    }

    override suspend fun start(){
        createTaskNotification(task, "Start", EventType.START, SeverityLevel.INFO)
        subscribeToHandlerUpdates()
    }
    internal suspend  fun systemInfo(type : EventType,  severity: SeverityLevel, message: String = ""){
        createTaskNotification(task, message, type,  severity)
    }

    override fun echo(message: String) {
        println(message)
    }
    override suspend fun info(message: String) {
        createTaskNotification(task, message, EventType.MESSAGE,  SeverityLevel.INFO)
    }

    override suspend  fun warn(message: String){
        createTaskNotification(task, message,  EventType.MESSAGE, SeverityLevel.WARNING)
    }
    override suspend fun error(ex: Throwable, optMessage: String) {
        val str = "${ex.message.toString()} $optMessage"
        createTaskNotification(task, str,  EventType.MESSAGE, SeverityLevel.EXCEPTION)
    }

}