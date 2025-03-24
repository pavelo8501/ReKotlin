package po.lognotify.eventhandler.classes

import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.eventhandler.exceptions.UnmanagedException
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



class StaticsHelper(val moduleName: String): StaticsHelperProvider{

    val RED = "\u001B[31m"
    val YELLOW = "\u001B[33m"
    val BLUE = "\u001B[34m"
    val MAGENTA = "\u001B[35m"
    val RESET = "\u001B[0m"

    override fun unhandledMsg(exception: Throwable): String{
        return "Unhandled exception in $moduleName. ${exception.message.toString()}"
    }
    override fun handledMsg(exception: Throwable): String{
        return "$RED Handled exception in $moduleName. Propagated to parent. ${exception.message.toString()} $RESET"
    }
    override fun msg(msg: String, ex: ProcessableException): String{
        return "$msg in $moduleName. ${ex.message}"
    }
    fun echo(ex: Throwable, message: String = ""){
        val baseMsg = "$moduleName raised an exception  ${ex.message}."
        when(ex){
            is ProcessableException ->{
                println("$RED $baseMsg Exception type: ProcessableException, Stack :  ${ex.stackTrace} $RESET $message")
            }
            is UnmanagedException ->{
                println("$RED $baseMsg Exception type: UnmanagedException, Stack :  ${ex.stackTrace} $RESET $message")
            }
        }
    }
    fun warn(message: String){
        println("$YELLOW  $message $RESET")
    }

    override fun newInfo(msg: String):Event { return  Event(moduleName, msg, SeverityLevel.INFO)}
    override fun newTask(msg: String):Task { return  Task(moduleName, msg)}
    override fun newWarning(msg: String):Task { return  Task(moduleName, msg)}
    override fun newWarning(ex:  ProcessableException):Task { return  Task(moduleName, ex.message)}

}