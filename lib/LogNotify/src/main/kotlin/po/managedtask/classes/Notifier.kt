package po.managedtask.classes

import po.managedtask.enums.ColourEnum
import po.managedtask.helpers.StaticsHelperProvider

interface CanNotify{
    fun echo(result : TaskResult<*>, message: String)
    fun info(result : TaskResult<*>, message: String)
    fun warn(result : TaskResult<*>, message: String)
    fun error(result : TaskResult<*>, ex: Throwable, optMessage: String)
}

class Notifier(val helper : StaticsHelperProvider) : StaticsHelperProvider by helper ,  CanNotify{

    override fun echo(result : TaskResult<*>, message: String) {
        val formatted = formatEcho(message)
        println(formatted)
    }

    override fun info(result : TaskResult<*>, message: String){
        result.addInfo(message)
        val formattedString = formatInfo(message)
        println(formattedString)
    }

    override fun warn(result : TaskResult<*>, message: String) {
        result.addWarning(message)
        val formattedString = formatWarn(message)
        println(formattedString)
    }
    override fun error(result : TaskResult<*>, ex: Throwable, optMessage: String) {
        val str = "${ex.message.toString()} $optMessage"
        result.addException(str)
        val formattedString = formatError(ex, optMessage)
        println(formattedString)
    }

}