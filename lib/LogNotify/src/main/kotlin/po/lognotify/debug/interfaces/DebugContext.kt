package po.lognotify.debug.interfaces

import po.lognotify.debug.models.InputParameter

internal interface DebugContext {

     val inputParams: MutableList<InputParameter>

}