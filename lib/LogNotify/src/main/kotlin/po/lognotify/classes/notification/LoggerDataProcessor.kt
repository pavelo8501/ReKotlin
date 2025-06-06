package po.lognotify.classes.notification

import po.lognotify.classes.notification.enums.EventType
import po.lognotify.classes.notification.models.ConsoleBehaviour
import po.lognotify.classes.notification.models.NotifyConfig
import po.lognotify.classes.notification.models.TaskData
import po.lognotify.classes.task.RootTask
import po.lognotify.classes.task.Task
import po.lognotify.classes.task.TaskBase
import po.lognotify.enums.SeverityLevel
import po.misc.data.console.PrintableTemplate
import po.misc.data.processors.DataProcessorBase

class LoggerDataProcessor(
    val task : TaskBase<*, *>,
    override val topEmitter: LoggerDataProcessor?
) : DataProcessorBase() {

    enum class LoggerProcessorType{ Root, Task }
    var config : NotifyConfig = NotifyConfig()

    var processorType: LoggerProcessorType = LoggerProcessorType.Task

    init {
        when(task){
            is RootTask -> {
                processorType = LoggerProcessorType.Root
            }
            is Task<*, *>->{
              config =  task.hierarchyRoot.dataProcessor.config
            }
        }
        provideMuteCondition(::setMuteConditions)
        provideOutputSource(::onTaskOutput)
    }

    private fun setMuteConditions(data: TaskData): Boolean{
      return when(config.console){
            ConsoleBehaviour.Mute ->true
            ConsoleBehaviour.FullPrint -> false
            ConsoleBehaviour.MuteInfo -> data.severity == SeverityLevel.INFO
            ConsoleBehaviour.MuteNoEvents -> {
                //Should refactor later to lookup for events in the task chain
                false
            }
        }
    }

    private fun onTaskOutput(message: String){
        println(message)
    }

    private fun createData(message: String, severity: SeverityLevel, template:  PrintableTemplate<TaskData>):TaskData{
        val data =  TaskData(
            taskKey = task.key,
            config = task.config,
            timeStamp = task.executionTimeStamp,
            message = message,
            severity = severity
        )
        processRecord(data, template)
        return data
    }

    fun systemEvent(eventType : EventType, message: String? = null):TaskData{
       return when(eventType){
            EventType.START->{
                createData("", SeverityLevel.INFO, TaskData.Header)
            }
            EventType.STOP -> {
                createData("", SeverityLevel.INFO, TaskData.Footer)
            }
            EventType.ExceptionRaised,
            EventType.ExceptionRethrown,
            EventType.ExceptionReachedTop -> {
                warn(message?: EventType.ExceptionReachedTop.toString(), task)
            }
            else -> {
                TODO("Not yet implemented.")
            }
        }
    }

    fun info(message: String): TaskData {
        return createData(message, SeverityLevel.INFO, TaskData.Message)
    }
    fun <R> info(message: String, task: TaskBase<*, R>): TaskData{
        return  createData(message, SeverityLevel.INFO, TaskData.Message)
    }

    fun warn(message: String): TaskData {
        return createData(message, SeverityLevel.WARNING,  TaskData.Message)
    }
    internal fun <R> warn(message: String, task: TaskBase<*, R>): TaskData{
        return  createData(message, SeverityLevel.WARNING, TaskData.Message)
    }
    fun warn(th: Throwable, message: String): TaskData {
        return createData("$message ${th.message.toString()}", SeverityLevel.WARNING,  TaskData.Message)
    }
    internal fun <R> error(message: String, task: TaskBase<*, R>): TaskData{
        return  createData(message, SeverityLevel.EXCEPTION, TaskData.Message)
    }
    internal fun <R> debug(message: String, where: String,  task: TaskBase<*, R>): TaskData{
        return  createData("Debug: ${where} -> ${message}" , SeverityLevel.INFO, TaskData.Debug)
    }

}