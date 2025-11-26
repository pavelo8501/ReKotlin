package po.misc.data.logging.processor

import po.misc.callbacks.signal.listen
import po.misc.context.tracable.TraceableContext
import po.misc.counters.AccessJournal
import po.misc.data.helpers.orDefault
import po.misc.data.helpers.replaceIfNull
import po.misc.data.logging.Loggable
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.processor.settings.ProcessorConfig
import po.misc.debugging.ClassResolver
import po.misc.types.ClassHierarchyMap
import po.misc.types.k_class.simpleOrAnon
import kotlin.reflect.KClass

class LogForwarder(
    val config : ProcessorConfig = ProcessorConfig()
): TraceableContext {

    enum class RecordType {
        Lookup,
        Register,
        RegisterOK,
        RegisterFail,
        LookupSuccess,
        LookupFailure,
        RemoveSuccess,
        RemoveFailure
    }

    data class HandlerRegistration(
        val baseClassHandled: KClass<out StructuredLoggable>,
        val handler: LogHandler,
        val hierarchyMap: ClassHierarchyMap
    )

    internal val handlerRegistrations = mutableListOf<HandlerRegistration>()

    val accessJournal: AccessJournal<RecordType> = AccessJournal(ClassResolver.instanceInfo(this), RecordType.Lookup)

    private fun notifyOnAddHandler(text: String, existent: LogHandler, newLogHandler: LogHandler?){
        if(config.notifyOverwrites){
            val newHandlerName : String? = if(newLogHandler != null){
                ClassResolver.instanceName(newLogHandler)
            }else{
                null
            }
            val text = "$text ${ClassResolver.instanceName(existent)} ${newHandlerName.orDefault("") { " by $it"}}"
            notify(outputImmediately = true, "UseHandler", text, NotificationTopic.Warning)
        }
    }
    private fun notifyHandling(text: String, loggable: StructuredLoggable?){
       val message = loggable?.let {
            val className = it::class.simpleOrAnon
            "$text for class $className. While processing $it"
        }?:run {
            text
        }
        notify(outputImmediately = true, "Data handling", message, NotificationTopic.Warning)
    }

    private fun resolveAllowOverwrite(allow: Boolean): Boolean{
        if(allow){
            return true
        }else{
            return config.notifyOverwrites
        }
    }

    private fun listenForCompletion(handler: LogHandler){
        listen(handler.completionSignal){handler->
            removeHandler(handler)
        }
    }

    internal fun getHandlerRegistration(msgClass: KClass<out StructuredLoggable>): HandlerRegistration? {
        var suitableRegistration: HandlerRegistration? = null
        var minDistance = Int.MAX_VALUE
        for (registration in handlerRegistrations){
            val distance = registration.hierarchyMap.getClassDistance(msgClass)
            if(distance == -1){
                continue
            }
            if(distance < minDistance){
                suitableRegistration = registration
                minDistance = distance
            }
        }
        return suitableRegistration
    }

    fun getHandlerFor(msgClass: KClass<out StructuredLoggable>): LogHandler? {
        val handingClass = msgClass.simpleOrAnon
        val record = accessJournal.register("Requesting handler for $handingClass")
        var suitableRegistration: HandlerRegistration? = null
        var minDistance = Int.MAX_VALUE
        for (registration in handlerRegistrations){
            val distance = registration.hierarchyMap.getClassDistance(msgClass)
            if(distance == -1){
                continue
            }
            if(distance < minDistance){
                suitableRegistration = registration
                minDistance = distance
            }
        }
        if(suitableRegistration != null){
            val handlerName = ClassResolver.instanceName(suitableRegistration.handler)
            record.resultOK(RecordType.LookupSuccess, "Handler $handlerName found as subtype $handingClass")
            return suitableRegistration.handler
        }else{
            val availableHandlerClasses = handlerRegistrations.joinToString { it.baseClassHandled.simpleOrAnon }
            record.resultFailure(RecordType.LookupFailure, "No handler found. Current registered handlers for $availableHandlerClasses")
            return null
        }
    }

    fun getHandlerFor(data: StructuredLoggable):  LogHandler? {
        return getHandlerFor(data::class)
    }


    /**
     * Overload to lookup handlers by their actual class
     */
    fun getHandler(handlerClass: KClass<out  LogHandler>):  LogHandler?{
        val found = handlerRegistrations.firstOrNull { it.handler::class == handlerClass }
        return found?.handler
    }

    fun registerHandler(
        dataHandler: LogHandler,
        messageBaseClass: KClass<out StructuredLoggable>,
        allowOverwrites: Boolean = true
    ) : HandlerRegistration?{

        val hierarchyMap = ClassHierarchyMap(messageBaseClass, config.maxHandlerHierarchyDepth, stopBefore = Loggable::class)
        val messageBaseClassName = messageBaseClass.simpleOrAnon
        val handlerName = ClassResolver.instanceInfo(dataHandler).instanceName
        val record = accessJournal.register(RecordType.Register, "Registering handler for $messageBaseClassName")

        val registration = HandlerRegistration(messageBaseClass, dataHandler, hierarchyMap)
        val existentRegistration = getHandlerRegistration(messageBaseClass)
        if(existentRegistration != null){
            if(allowOverwrites){
                handlerRegistrations.remove(existentRegistration)
                record.resultOK(RecordType.RegisterOK, "Handler $handlerName registered(overwriting) for base class $messageBaseClassName")
                listenForCompletion(registration.handler)
                handlerRegistrations.add(registration)
                return registration
            }else{
                record.resultFailure(RecordType.RegisterFail, "Handler $handlerName registration denied. Already exists")
                return null
            }
        }else{
            record.resultOK(RecordType.RegisterOK, "Handler $handlerName registered for base class $messageBaseClassName")
            listenForCompletion(registration.handler)
            handlerRegistrations.add(registration)
            return registration
        }
    }

    fun registerHandler(
        dataHandler: LogHandler,
        allowOverwrites: Boolean = true
    ) : HandlerRegistration? = registerHandler(dataHandler, dataHandler.targetClassHandled, allowOverwrites)

    fun registerHandlers(
        dataHandler: List<LogHandler>,
        allowOverwrites: Boolean = true
    ) : Boolean {
        var success = true
        dataHandler.forEach {
           val config = registerHandler(it, allowOverwrites)
            if(config == null){
                success =false
            }
        }
        return success
    }


    /**
     * Registers a [dataHandler] using its declared [LogHandler.baseClassHandled] as
     * the target key.
     *
     * Behaves exactly like the typed [useHandler] variant: returns the effective handler
     * if registration succeeds, or `null` if an existing handler could not be replaced.
     * @param dataHandler The handler to register.
     * @param messageBaseClass The key associated with the handler.
     */
    fun useHandler(
        dataHandler: LogHandler,
        messageBaseClass: KClass<out StructuredLoggable>,
        allowSubstitution: Boolean = true
    ) : HandlerRegistration{

        val hierarchyMap = ClassHierarchyMap(messageBaseClass, config.maxHandlerHierarchyDepth, stopBefore = Loggable::class)
        val messageBaseClassName = messageBaseClass.simpleOrAnon
        val handlerName = ClassResolver.instanceInfo(dataHandler).instanceName
        val record = accessJournal.register(RecordType.Register, "Registering handler for $messageBaseClassName")
        val registration = HandlerRegistration(messageBaseClass, dataHandler, hierarchyMap)
        val existentRegistration = getHandlerRegistration(messageBaseClass)
        if(existentRegistration != null){
            if(allowSubstitution){
                handlerRegistrations.remove(existentRegistration)
                record.resultOK(RecordType.RegisterOK, "Handler $handlerName registered(overwriting) for base class $messageBaseClassName")
                listenForCompletion(registration.handler)
                handlerRegistrations.add(registration)
                return registration
            }else{
                record.resultFailure(RecordType.RegisterFail, "Handler $handlerName registration denied. Already exists")
                return existentRegistration
            }
        }else{
            record.resultOK(RecordType.RegisterOK, "Handler $handlerName registered for base class $messageBaseClassName")
            listenForCompletion(registration.handler)
            handlerRegistrations.add(registration)
            return registration
        }
    }

    /**
     * Registers a [dataHandler] using its declared [LogHandler.baseClassHandled] as
     * the target key.
     *
     * Behaves exactly like the typed [useHandler] variant: returns the effective handler
     * if registration succeeds, or `null` if an existing handler could not be replaced.
     *
     * @param dataHandler The handler to register.
     */
    fun useHandler(
        dataHandler: LogHandler,
        allowSubstitution: Boolean = true
    ) : HandlerRegistration = useHandler(dataHandler, dataHandler.targetClassHandled, allowSubstitution)

    fun handle(loggableRecord: StructuredLoggable): Boolean {
        val loggableClass = loggableRecord::class
        val handler = getHandlerFor(loggableClass)
        if(handler != null){
            handler.processRecord(loggableRecord)
            return true
        }else{
            if(config.notifyDataUnhandled){
                notifyHandling("Unhandled. No handler key", loggableRecord)
            }
        }
        return false
    }





    /**
     * Removes the handler registered for the given [handlingClass], if any.
     *
     * @param handlingClass The key associated with the handler to remove.
     * @return `true` if a handler was removed, or `false` if no handler existed.
     */
    fun removeHandler(handlingClass: KClass<out StructuredLoggable>): Boolean{
        val record = accessJournal.register("Removing handler for ${handlingClass.simpleOrAnon}")
        val handlerRegistration = getHandlerRegistration(handlingClass)
        return if(handlerRegistration == null){
            record.resultFailure(RecordType.RemoveFailure, "No handler to remove.")
            false
        }else{
            val isRemoved = handlerRegistrations.remove(handlerRegistration)
            if(isRemoved){
                record.resultOK(RecordType.RemoveSuccess)
            }else{
                record.resultFailure(RecordType.RemoveFailure, "Registration found but not removed")
            }
            isRemoved
        }
    }

    /**
     * Removes the handler registered for the given [handler], if any.
     *
     * @param handler The key associated with the handler to remove.
     * @return `true` if a handler was removed, or `false` if no handler existed.
     */
    fun removeHandler(handler: LogHandler):Boolean = removeHandler(handler.targetClassHandled)

    fun removeHandlers(): Unit = handlerRegistrations.clear()
}


inline fun <reified T:StructuredLoggable> LogProcessorBase<T>.getHandler(): LogHandler? = logForwarder.getHandlerFor(T::class)
inline fun <reified T:StructuredLoggable> LogForwarder.getHandler(): LogHandler? = getHandlerFor(T::class)