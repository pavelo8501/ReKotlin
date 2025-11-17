package po.misc.data.logging

import po.misc.data.logging.models.ContextMessage
import po.misc.data.processors.SeverityLevel


 class EmitterConfig {
     var verbosity: Verbosity = Verbosity.Info
         private set

     fun setVerbosity(verbosity: Verbosity): EmitterConfig {
         this.verbosity = verbosity
         return this
     }

     internal var onMessageCallback: ((ContextMessage)-> Unit)? = null
     fun onMessage(callback: (ContextMessage)-> Unit):EmitterConfig{
         onMessageCallback = callback
         return this
     }
 }


open class LogEmitterClass(
    open val host: Any,
    val configurator:(EmitterConfig.()-> Unit)? = null
): EmittableClass {

    val config : EmitterConfig = EmitterConfig()
    val verbosity: Verbosity get() = config.verbosity

    init {
        configurator?.invoke(config)
    }

    override fun info(message: String){
        if(verbosity != Verbosity.Warnings){
            host.notify(message)
        }
    }

    override fun warn(message: String){
        host.notify(message, SeverityLevel.WARNING)
    }

    override fun error(message: String){
        host.notify(message, SeverityLevel.EXCEPTION)
    }
}