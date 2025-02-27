package po.lognotify.eventhandler.classes

import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.eventhandler.models.Event
import po.lognotify.eventhandler.models.Task
import po.lognotify.shared.enums.SeverityLevel

interface StaticsHelperProvider {
    fun unhandledMsg(exception: Throwable): String
    fun handledMsg(exception: Throwable): String
    fun msg(msg: String, ex: ProcessableException): String
    fun newInfo(msg: String):Event
    fun newTask(msg: String):Task
    fun newWarning(msg: String):Task
    fun newWarning(ex:  ProcessableException):Task
}

abstract class StaticsHelper: StaticsHelperProvider{

    abstract val moduleName: String
    override fun unhandledMsg(exception: Throwable): String{
        return "Unhandled exception in $moduleName. ${exception.message.toString()}"
    }
    override fun handledMsg(exception: Throwable): String{
        return "Handled exception in $moduleName. Propagated to parent. ${exception.message.toString()}"
    }
    override fun msg(msg: String, ex: ProcessableException): String{
        return "$msg in $moduleName. ${ex.message}"
    }

    override fun newInfo(msg: String):Event { return  Event(moduleName, msg, SeverityLevel.INFO)}
    override fun newTask(msg: String):Task { return  Task(moduleName, msg)}
    override fun newWarning(msg: String):Task { return  Task(moduleName, msg)}
    override fun newWarning(ex:  ProcessableException):Task { return  Task(moduleName, ex.message)}

}