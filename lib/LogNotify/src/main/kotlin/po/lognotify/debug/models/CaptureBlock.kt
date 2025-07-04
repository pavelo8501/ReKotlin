package po.lognotify.debug.models

import po.lognotify.debug.interfaces.DebugProvider

class CaptureBlock<INPUT: Any>(
    val parameter:INPUT,
    internal val inputParameter: InputParameter
): DebugProvider {

    override val inputParams: MutableList<InputParameter> =  mutableListOf()

}