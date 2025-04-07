package po.lognotify.classes.notification

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import po.lognotify.classes.ManagedResult
import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.enums.InfoProvider
import po.lognotify.classes.notification.models.Notification
import po.lognotify.classes.task.ResultantTask
import po.lognotify.classes.task.TaskSealedBase
import po.lognotify.enums.SeverityLevel
import po.lognotify.helpers.StaticsHelper

interface NotificationProvider{

    suspend fun start()

    fun echo(message: String)
    suspend fun info(message: String)
    fun warn(message: String, result : ManagedResult<*>?= null)
    fun error(ex: Throwable, optMessage: String)

    suspend fun echo(task: ResultantTask, message: String)
    suspend fun warn(task: ResultantTask, message: String)
    suspend fun <T : ResultantTask>  T.error(ex: Throwable, optMessage: String)
}

class Notifier(
    private val task: TaskSealedBase<*>,
    private val helper: StaticsHelper = StaticsHelper(task.taskName)
) : NotificationProvider{

    private val _notification = MutableSharedFlow<Notification>(extraBufferCapacity = 64)
    val notification: SharedFlow<Notification> = _notification.asSharedFlow()

    init {

    }

    suspend fun emit(notification : Notification){
        _notification.emit(notification)
    }

    fun toConsole(notification : Notification){

        var  header  =  helper.systemPrefix(
            "/Nested : ${notification.taskNestingLevel}/${notification.eventType.name}")
        when(notification.severity){
            SeverityLevel.INFO -> {
                println("$header ${ helper.formatInfo(notification.message)}")
            }
            SeverityLevel.WARNING -> {

                println("$header ${helper.formatWarn(notification.message)}")
            }
            SeverityLevel.EXCEPTION -> {
                helper.formatError(notification.message)
                println("$header ${helper.formatError(notification.message)}")
            }
        }
    }

    private suspend fun emitThrowerNotification(notification : Notification){
        toConsole(notification)
        emit(notification)
    }

    private suspend fun createTaskNotification(task : ResultantTask, message: String, severity: SeverityLevel){
        val notification = Notification(
            task.taskName,
            task.nestingLevel,
            EventType.SYSTEM_MESSAGE,
            severity,
            message,
            InfoProvider.TASK
        )
        val str =  notification.toFormattedString()
        println(str)
        emit(notification)
    }

    private suspend fun emitHandlerNotification(notification : Notification){
        toConsole(notification)
        emit(notification)
    }

    suspend fun subscribeToThrowerUpdates(){
        withContext(task.context) {
            task.taskHelper.exceptionThrower.subscribeThrowerUpdates{
                emitThrowerNotification(it)
            }
        }
    }

    suspend fun subscribeToHandlerUpdates(){
        withContext(task.context) {
            task.taskHelper.exceptionHandler.subscribeHandlerUpdates(){
                emitHandlerNotification(it)
            }
        }
    }

    override suspend fun start(){
        subscribeToThrowerUpdates()
        subscribeToHandlerUpdates()
    }


    suspend fun systemInfo(message: String, severity: SeverityLevel, type : EventType){

        if(type == EventType.START){
             createTaskNotification(task, message, severity)
        }

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
        val formatted = helper.formatEcho(message)
        println(formatted)
    }

    override suspend fun info(message: String) {

        createTaskNotification(task, message , SeverityLevel.INFO)
        val formattedString = helper.formatInfo(message)
        println(formattedString)
    }

    override fun warn(message: String, result: ManagedResult<*>?) {

        val formattedString = helper.formatWarn(message)
        println(formattedString)
    }

    override suspend  fun warn(task: ResultantTask, message: String){
        createTaskNotification(task, message, SeverityLevel.WARNING)
    }

    override suspend fun <T : ResultantTask> T.error(
        ex: Throwable,
        optMessage: String
    ) {
        TODO("Not yet implemented")
    }

    override fun error(ex: Throwable, optMessage: String) {
        val str = "${ex.message.toString()} $optMessage"
        println(str)
    }
    override suspend fun echo(task: ResultantTask, message: String) {
        var formatted = "${helper.taskPrefix(task)} | 🗣️ $message"
        println(formatted)
    }



}