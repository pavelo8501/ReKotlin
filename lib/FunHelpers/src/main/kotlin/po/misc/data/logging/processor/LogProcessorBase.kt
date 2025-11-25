package po.misc.data.logging.processor

import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.output.output
import po.misc.data.logging.Loggable
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.Verbosity
import po.misc.data.logging.factory.toLogMessage
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.models.Notification
import po.misc.data.logging.parts.DebugMethod
import po.misc.data.logging.log_subject.DebugSubject
import po.misc.data.logging.procedural.ProceduralRecord
import po.misc.data.logging.processor.settings.ProcessorConfig
import po.misc.data.logging.track
import po.misc.data.styles.Colour
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass


abstract class LogProcessorBase<T: StructuredLoggable>(
    protected var hostName: String,
    val messageTypeToken: TypeToken<T>,
): TraceableContext {


    abstract var verbosity: Verbosity


    val processorConfig = ProcessorConfig()

    var onDataInterception : ((T)-> Unit)? = null
        protected set
    var shouldStoreRecords: Boolean = true
        protected set

    var generalMute: Boolean = false
        protected set

    var debugMode: Boolean = false

    val name: String get() =  "LogProcessor on $hostName"

    val trackMessage : (String, StructuredLoggable) -> String = {method, data->
        "$name -> $method(${data})"
    }

    var activeRecord: T? = null
        protected set

    var activeUnresolved: StructuredLoggable? = null
        protected set

    private val logRecordsBacking: MutableList<T> = mutableListOf()
    val logRecords : List<T>  = logRecordsBacking

    @PublishedApi
    internal val logForwarder: LogForwarder =  LogForwarder()

    abstract fun outputOrNot(data: Loggable)
    abstract fun handleUnAssigned(message: Loggable)

    private fun outputDebud(subject: DebugSubject){
        "${subject.subjectName} ${subject.subjectText}".output(this)
    }

    private fun storeData(data: T, tryHandle: Boolean = true){

        logRecordsBacking.add(data)
        activeRecord = data

//        if(tryHandle){
//            if(!handleStructured(data) ){
//                logRecordsBacking.add(data)
//                activeRecord = data
//            }
//        }else{
//            logRecordsBacking.add(data)
//            activeRecord = data
//        }
    }

    private fun handleStructured(loggable: StructuredLoggable): Boolean{
        return logForwarder.handle(loggable)
    }

    protected open fun verbosityLevelMet(loggable: Loggable): Boolean{
        return loggable.topic >= verbosity.minTopic
    }

    fun useHandler(
        dataHandler: LogHandler,
        handlingClass: KClass<out StructuredLoggable>,
    ):  LogHandler? = logForwarder.registerHandler(dataHandler, handlingClass)?.handler

    fun useHandler(
        dataHandler: LogHandler,
    ) :  LogHandler? = logForwarder.registerHandler(dataHandler)?.handler

    fun removeDataHandler(handlingClass: KClass<out StructuredLoggable>): Boolean =
        logForwarder.removeHandler(handlingClass)

    fun log(loggable: Loggable){
        val kClass = messageTypeToken.kClass
        val casted = loggable.safeCast(kClass)
        if(casted != null){
            logData(casted)
            if(debugMode) outputDebud(DebugMethod.methodName("log2"))
            return
        }
        val initialStructured = logRecords.firstOrNull()?:activeUnresolved
        if(initialStructured != null) {
            when (loggable) {
                is ProceduralRecord -> {
                    if(debugMode) outputDebud(DebugMethod.methodName("log2", "${loggable::class}  -> handleStructured"))
                    "log ProceduralRecord branch hit".output(this, Colour.Yellow)
                }
                is LogMessage ->{
                    initialStructured.addRecord(loggable)
                    if(debugMode) outputDebud(DebugMethod.methodName("log2", "${loggable::class} to entries"))
                }
                is Notification -> {
                    initialStructured.addRecord(loggable)
                    if(debugMode) outputDebud(DebugMethod.methodName("log2", "Notification to entries"))
                }
            }
            return
        }else{
            if(loggable is StructuredLoggable){
                handleStructured(loggable)
                if(debugMode){ outputDebud(DebugMethod.methodName("log2", "${loggable::class} to activeUnresolved")) }
            }else{
                val converted =  loggable.toLogMessage()
                handleStructured(converted)
            }
        }
    }

    fun logData(data: T, noOutput: Boolean = false):T{

        val shouldOutput = verbosityLevelMet(data)

        if(shouldOutput){
            val handlerExists = logForwarder.getHandlerFor(data)
            if(handlerExists != null){
                handlerExists.processRecord(data)
                return data
            }else{
                if(shouldStoreRecords){
                    storeData(data)
                }
                if(!noOutput && !generalMute ){
                    outputOrNot(data)
                }
                onDataInterception?.let { callback->
                    callback.invoke(data)
                }
            }
        }else{
            storeData(data, tryHandle = false)
        }
        return data
    }

    fun collectData(keepData: Boolean, callback: (StructuredLoggable)-> Unit){
        shouldStoreRecords = keepData
        onDataInterception = callback
    }

    fun collectData(keepData: Boolean, setGeneralMute: Boolean,  callback: (StructuredLoggable)-> Unit){
        generalMute = setGeneralMute
        shouldStoreRecords = keepData
        onDataInterception = callback
    }

    fun dropCollector(){
        shouldStoreRecords = true
        onDataInterception = null
    }

    fun dropCollector(dropGeneralMute: Boolean){
        generalMute = !dropGeneralMute
        shouldStoreRecords = true
        onDataInterception = null
    }


    fun clear(){
         logRecordsBacking.clear()
    }
}