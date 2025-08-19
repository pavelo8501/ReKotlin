package po.misc.data.logging

import po.misc.data.processors.SeverityLevel

class LogEmitterClass(
    val host: Any
) : EmittableClass {

    override fun warn(message: String){
        host.notify(message, SeverityLevel.WARNING)
    }
    override fun info(message: String){
        host.notify(message)
    }
    override fun error(message: String){
        host.notify(message, SeverityLevel.EXCEPTION)
    }



}