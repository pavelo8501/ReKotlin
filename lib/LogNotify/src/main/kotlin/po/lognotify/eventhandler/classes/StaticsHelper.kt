package po.lognotify.eventhandler.classes

import po.lognotify.eventhandler.interfaces.HandlerStatics
import po.lognotify.eventhandler.models.Event
import po.lognotify.shared.enums.SeverityLevel

interface StaticsHelperProvider {
    fun unhandledMsg(exception: Throwable): String
    fun handledMsg(exception: Throwable): String
    fun newInfo(msg: String):Event
    fun newEvent(msg: String):Event
}

abstract class StaticsHelper: StaticsHelperProvider{
    abstract val moduleName: String
    override fun unhandledMsg(exception: Throwable): String{
        return "Unhandled exception in $moduleName. ${exception.message.toString()}"
    }
    override fun handledMsg(exception: Throwable): String{
        return "Handled exception in $moduleName. Propagated to parent. ${exception.message.toString()}"
    }
    override fun newInfo(msg: String):Event { return  Event(moduleName, msg, SeverityLevel.INFO)}
    override fun newEvent(msg: String):Event { return  Event(moduleName, msg, SeverityLevel.EVENT)}

}