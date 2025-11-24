package po.misc.context.log_provider

import po.misc.context.component.Component
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.processor.LogForwarder
import po.misc.data.logging.processor.LogHandler
import po.misc.data.logging.processor.parts.EmitHub
import po.misc.data.logging.processor.LogProcessor
import kotlin.reflect.KClass



interface LogProvider : Component {

    val logProcessor: LogProcessor<out LogProvider, out StructuredLoggable>


    override fun notify(logMessage: LogMessage): StructuredLoggable{
        logProcessor.loader.loaderRoutine()
        logProcessor.log(logMessage)
        return logMessage
    }

    fun <SL: StructuredLoggable> getHandlerFor(messageClass: KClass<SL>):  LogHandler? =
        forwarder.getHandlerFor(messageClass)

    fun  getHandler(handlerClass: KClass<out  LogHandler>):  LogHandler? = forwarder.getHandler(handlerClass)


    fun useLogHandler(
        handler: LogHandler
    ): LogForwarder.HandlerRegistration?{
         return forwarder.registerHandler(handler)
    }

    fun useLogHandler(
        logProcessor: LogProcessor<*,  out StructuredLoggable>
    ): Boolean{
        val handlers = logProcessor.logForwarder.handlerRegistrations.map { it.handler }
        return forwarder.registerHandlers(handlers)
    }

    fun useLogHandler(
        logProvider: LogProvider
    ): Boolean{
        val handlers = logProvider.logProcessor.logForwarder.handlerRegistrations.map { it.handler }
        return forwarder.registerHandlers(handlers)
    }

    companion object: EmitHub()
}

