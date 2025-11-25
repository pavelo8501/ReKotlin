package po.misc.data.logging.processor.parts

import po.misc.context.component.Component
import po.misc.context.log_provider.LogProvider
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.processor.LogHandler
import po.misc.data.logging.processor.LogProcessor
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class ProcessorLoader<H: Component, SL: StructuredLoggable>(
    private val logProcessor: LogProcessor<H, SL>
): TraceableContext {


    private val handlerMap = ConcurrentHashMap<KClass<SL>, LogHandler>()

    init {
        loaderRoutine()
    }

    private fun addNewHandler(messageClass: KClass<SL>, handler: LogHandler):LogHandler{
        handlerMap[messageClass] = handler
        logProcessor.useHandler(handler)
        handler.completionSignal.onSignal(this) {
            handlerMap.values.remove(it)
        }
        return handler
    }

    fun loaderRoutine(): LogHandler?{
        val host = logProcessor.host
        val messageClass = logProcessor.messageTypeToken.kClass
        return when(host){
            is LogProvider -> {
                handlerMap[messageClass] ?:run {
                    val handler = host.getHandlerFor(messageClass)
                    if(handler != null){
                        addNewHandler(messageClass, handler)
                    }else{
                        null
                    }
                }
            }
            else -> null
        }
    }
}