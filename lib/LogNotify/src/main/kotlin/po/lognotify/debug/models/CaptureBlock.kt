package po.lognotify.debug.models

import po.lognotify.debug.interfaces.DebugProvider

class CaptureBlock<INPUT: Any>(
    val parameter:INPUT,
): DebugProvider {

    override val inputParams: MutableList<InputParameter> =  mutableListOf()

}