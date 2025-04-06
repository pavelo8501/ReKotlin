package po.managedtask.classes.notification

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import po.managedtask.classes.ManagedResult
import po.managedtask.classes.task.TaskSealedBase
import po.managedtask.enums.SeverityLevel
import po.managedtask.exceptions.ExceptionNotification
import po.managedtask.exceptions.HandlerNotification
import po.managedtask.helpers.StaticsHelperProvider
import java.time.LocalDateTime

interface NotificationProvider{

    suspend fun start()

    fun echo(message: String)
    fun info(message: String, result : ManagedResult<*>?= null)
    fun warn(message: String, result : ManagedResult<*>?= null)
    fun error(ex: Throwable, optMessage: String)
}


class Notifier(
    private val task: TaskSealedBase<*>,
    private val helper: StaticsHelperProvider = task.helper
) : StaticsHelperProvider by helper,  NotificationProvider{

    private val _notification = MutableSharedFlow<Notification>(extraBufferCapacity = 64)
    val notification: SharedFlow<Notification> = _notification.asSharedFlow()

    init {

    }

    suspend fun emit(notification : Notification){
        _notification.emit(notification)
    }

    fun toConsole(notification : Notification){
        var  header  =  helper.systemPrefix(
            "/Nested : ${notification.taskNestingLevel}/${notification.action.name}")
        when(notification.severity){
            SeverityLevel.INFO -> {
                println("$header ${ formatInfo(notification.message)}")
            }
            SeverityLevel.WARNING -> {

                println("$header ${formatWarn(notification.message)}")
            }
            SeverityLevel.EXCEPTION -> {
                formatError(notification.message)
                println("$header ${formatError(notification.message)}")
            }
        }
    }


    private suspend fun emitThrowerNotification(exceptionNotification : ExceptionNotification){
        task.key.nestingLevel
        val notification = Notification(
            exceptionNotification.taskName,
            task.key.nestingLevel,
            exceptionNotification.notifyTask,
            SeverityLevel.INFO,
            exceptionNotification.message,
            InfoProvider.EX_THROWER,
            exceptionNotification.toString(),
            LocalDateTime.now()
        )
        toConsole(notification)
        emit(notification)
    }

    private suspend fun emitHandlerNotification(exceptionNotification : HandlerNotification){

        val notification = Notification(
            exceptionNotification.taskName,
            exceptionNotification.nestingLevel,
            exceptionNotification.type,
            exceptionNotification.severity,
            exceptionNotification.message,
            InfoProvider.EX_HANDLER,
            exceptionNotification.toString(),
            LocalDateTime.now()
        )
        toConsole(notification)
        emit(notification)

    }

    suspend fun subscribeToThrowerUpdates(){
        withContext(task.context) {
            task.taskHelper.thrower.subscribeThrowerUpdates{
                emitThrowerNotification(it)
            }
        }
    }

    suspend fun subscribeToHandlerUpdates(){
        withContext(task.context) {
            task.taskHelper.handler.subscribeHandlerUpdates(){
                emitHandlerNotification(it)
            }
        }
    }

    override suspend fun start(){
        subscribeToThrowerUpdates()
        subscribeToHandlerUpdates()
    }

    suspend fun systemInfo(message: String, severity: SeverityLevel){

        when(severity){
            SeverityLevel.INFO -> {
                val formatted =  helper.formatSystemMsg(message)
                println(formatted)
            }
            SeverityLevel.WARNING -> {
                val prefix =  helper.formatSystemMsg(helper.libPrefix)
                println("$prefix ${helper.formatWarn(message)}")
            }
            SeverityLevel.EXCEPTION -> {

            }
        }
    }
    suspend fun systemInfo(message: String, exception : Throwable){
        val formatSystem=  helper.formatSystemMsg(message)
        val formatted =  helper.formatUnhandled(exception)
        println("$formatSystem  $formatted")
    }

    override fun echo(message: String) {
        val formatted = formatEcho(message)
        println(formatted)
    }

    override fun info(message: String, result: ManagedResult<*>?) {
        val formattedString = formatInfo(message)
        println(formattedString)
    }

    override fun warn(message: String, result: ManagedResult<*>?) {

        val formattedString = formatWarn(message)
        println(formattedString)
    }

    override fun error(ex: Throwable, optMessage: String) {
        val str = "${ex.message.toString()} $optMessage"
        println(str)
    }

}