package po.lognotify.classes.notification

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.models.ConsoleBehaviour
import po.lognotify.classes.notification.models.Notification
import po.lognotify.classes.notification.models.NotifyConfig
import po.lognotify.classes.notification.sealed.DataProvider
import po.lognotify.classes.notification.sealed.ProviderLogNotify
import po.lognotify.classes.notification.sealed.ProviderProcess
import po.lognotify.classes.notification.sealed.ProviderTask
import po.lognotify.classes.process.LoggProcess
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.enums.SeverityLevel
import po.lognotify.helpers.StaticHelper
import po.lognotify.models.TaskDispatcher
import po.misc.exceptions.CoroutineInfo

interface NotificationProvider{

    fun echo(message: String)
    fun info(message: String)
    fun error(ex: Throwable, optMessage: String)
    fun warn(message: String)
}

class RootNotifier(
    dispatcher: TaskDispatcher,
    coroutineInfo: CoroutineInfo?,
    config : NotifyConfig = NotifyConfig(),
): NotifierBase(ProviderLogNotify(dispatcher, coroutineInfo)){

}

class ProcessNotifier(
    config : NotifyConfig = NotifyConfig(),
    private val process: LoggProcess<*>,
) : NotifierBase(ProviderProcess(process)), NotificationProvider,  StaticHelper{

}

class Notifier(
    private val task: TaskSealedBase<*>,
) : NotifierBase(ProviderTask(task)), NotificationProvider, StaticHelper{



}

sealed class NotifierBase(
    private val provider: DataProvider
): StaticHelper, NotificationProvider {

    
    var config : NotifyConfig = NotifyConfig()

    private var hasRealEvents = false
    private val bufferedNotifications = mutableListOf<Notification>()
    private val notificationFlow = MutableSharedFlow<Notification>(
        replay = 10,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    val notifications: SharedFlow<Notification> = notificationFlow.asSharedFlow()

    private fun toConsole(notification: Notification) {

        when (notification.eventType) {
            EventType.START -> {
                val header = notification.header
                println(header)
            }

            EventType.STOP -> {
                val header = notification.footer
                println(header)
            }

            else -> {
                val formattedString = notification.getMessagePrefixed()
                println(formattedString)
            }
        }
    }

    protected fun emitNotification(notification: Notification) {
        when(config.console){
            ConsoleBehaviour.Mute->{ }
            ConsoleBehaviour.MuteInfo -> {
                if(notification.severity != SeverityLevel.INFO){
                   toConsole(notification)
                }
            }
            ConsoleBehaviour.MuteNoEvents -> {
                if(notification.provider is ProviderTask && notification.provider.nestingLevel == 0){
                    toConsole(notification) //If root task print header footer anyway
                }else{
                    if(hasRealEvents){
                        toConsole(notification)
                    }else{
                        bufferedNotifications.add(notification)
                    }
                }
                if(notification.severity != SeverityLevel.SYS_INFO){
                    hasRealEvents = true
                    bufferedNotifications.forEach {
                        toConsole(it)
                    }
                    bufferedNotifications.clear()
                }
            }
            ConsoleBehaviour.FullPrint -> {
                 toConsole(notification)
            }
        }
        CoroutineScope(Dispatchers.Default).launch {
            notificationFlow.emit(notification)
        }
    }

    private fun createTaskNotification(
        provider: DataProvider,
        message: String,
        type: EventType,
        severity: SeverityLevel
    ) {
        val notification = Notification(
            provider,
            type,
            severity,
            message)

        emitNotification(notification)
    }

    internal fun submitNotification(notification : Notification){
        emitNotification(notification)
    }

    fun setNotifierConfig(configuration : NotifyConfig){
        config = configuration
    }

    fun getNotifierConfig():NotifyConfig{
        return config
    }

    internal fun systemInfo(type : EventType,  severity: SeverityLevel, message: String = ""){
        createTaskNotification(provider, message, type,  severity)
    }

    internal fun systemInfo(eventType : EventType,  severity: SeverityLevel, process: LoggProcess<*>){
        when(eventType){
            EventType.START->{
                val message = "Process [${process.name}] id: ${process.identifiedAs} entered Task ${provider.name}"
                createTaskNotification(provider, message, eventType,  severity)
                info(message)
            }
            EventType.STOP -> {
                val message = "Process [${process.name}] id: ${process.identifiedAs} exit Task ${provider.name}"
                createTaskNotification(provider, message, eventType,  severity)
                info(message)
            }
            else -> {
                val message = "Process id: ${process.identifiedAs} unknown event type"
                createTaskNotification(provider, message, eventType,  severity)
            }
        }
    }

    override fun echo(message: String) {
        println(message)
    }
    override fun info(message: String) {
        createTaskNotification(provider, message, EventType.MESSAGE,  SeverityLevel.INFO)
    }

    override fun warn(message: String){
        createTaskNotification(provider, message,  EventType.MESSAGE, SeverityLevel.WARNING)
    }
    override fun error(ex: Throwable, optMessage: String) {
        val str = "${ex.message.toString()} $optMessage"
        createTaskNotification(provider, str,  EventType.MESSAGE, SeverityLevel.EXCEPTION)
    }

}