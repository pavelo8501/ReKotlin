package po.misc.data.logging.processor.parts

import po.misc.context.component.Component
import po.misc.context.log_provider.LogProvider
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.processor.LogHandler
import po.misc.data.logging.processor.LogProcessor

class ProcessorLoader<H: Component, SL: StructuredLoggable>(
    private val logProcessor: LogProcessor<H, SL>
) {

    init {
        loaderRoutine()
    }

    fun loaderRoutine(): LogHandler?{
        val host = logProcessor.host
        val messageClass = logProcessor.messageTypeToken.kClass
        return when(host){
            is LogProvider -> {
                val handler = host.getHandlerFor(messageClass)
                if(handler != null){
                    logProcessor.useHandler(handler)
                }
                handler
            }
            else -> null
        }
    }
}