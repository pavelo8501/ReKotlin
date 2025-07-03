package po.lognotify.debug.models

import po.lognotify.debug.interfaces.DebugContext

class CaptureBlock<INPUT: Any>(
    val parameter:INPUT,
    internal val inputParameter: InputParameter
): DebugContext {

    override val inputParams: MutableList<InputParameter> =  mutableListOf()

}