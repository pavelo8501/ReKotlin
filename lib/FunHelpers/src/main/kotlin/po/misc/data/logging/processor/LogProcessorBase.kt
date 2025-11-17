package po.misc.data.logging.processor

import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.helpers.output
import po.misc.data.logging.Loggable
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.Verbosity
import po.misc.data.logging.factory.toLogMessage
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.models.Notification
import po.misc.data.logging.parts.DebugMethod
import po.misc.data.logging.log_subject.DebugSubject
import po.misc.data.logging.parts.SubjectDebugState
import po.misc.data.logging.procedural.ProceduralRecord
import po.misc.data.logging.track
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import kotlin.collections.set
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


abstract class LogProcessorBase<T:StructuredLoggable>(
    open val host: Component,
    val typeToken: TypeToken<T>,
): TraceableContext{


    data class StateSnapshot(
        private val processor: LogProcessor<*, *>,
        val name : String = processor.name,
        val loggedMessageCount: Int = processor.logRecords.size,

        val shouldStoreRecords : Boolean = processor.shouldStoreRecords,
        val generalMute : Boolean  = processor.generalMute,
        val hasActiveRecord : Boolean = processor.activeRecord != null,
        val hasActiveUnresolved : Boolean = processor.activeUnresolved != null,
        val activeDataHandlers: List<KClass<*>>  = processor.dataHandlers.keys.toList()
    )



    var onDataInterception : ((T)-> Unit)? = null
        protected set
    var shouldStoreRecords: Boolean = true
        protected set

    var generalMute: Boolean = false
        protected set

    var debugMode: Boolean = false

    val verbosity : Verbosity get() {
        return host.componentID.verbosity
    }
    val name: String get() =  "LogProcessor on ${host.componentID.componentName}"

    val trackMessage : (String, StructuredLoggable) -> String = {method, data->
        "$name -> $method(${data})"
    }

    var activeRecord: T? = null
        protected set

    var activeUnresolved: StructuredLoggable? = null
        protected set

    private val logRecordsBacking: MutableList<T> = mutableListOf()
    val logRecords : List<T>  = logRecordsBacking

    protected val dataHandlers: MutableMap<KClass<out StructuredLoggable>, LogHandler> = mutableMapOf()

    abstract fun outputOrNot(data: Loggable)
    abstract fun handleUnAssigned(message: Loggable)

    private fun outputDebud(subject: DebugSubject){
        debug(subject.subjectName, subject.subjectText, outputImmediately = true)
    }

    private fun storeData(data: T){
        if(!handleStructured(data) ){
             data.track(this, "storeData" )
            logRecordsBacking.add(data)
            activeRecord = data
        }
    }

    private fun handleUnStructured(loggable: Loggable): Boolean{
        val handlerFound = dataHandlers.values.firstOrNull()
        if(handlerFound != null){
            handlerFound.processData(loggable.toLogMessage())
            return true
        }else{
            return false
        }
    }

    private fun handleStructured(loggable: StructuredLoggable): Boolean{
        val loggableClass = loggable::class
        if(debugMode) outputDebud(DebugMethod.methodName("handleStructured"))
        val filteredKeys =  dataHandlers.keys.filter { loggableClass.isSubclassOf(it) }
        if(filteredKeys.isNotEmpty()){
            filteredKeys.forEach { key->
                dataHandlers[key]?.processData(loggable)
            }
            return true
        }else{
            if (debugMode) outputDebud(SubjectDebugState.provideState(this, "handler found."))
            return false
        }
    }

    fun useHandler(
        dataHandler: LogHandler,
        handlingClass: KClass<out StructuredLoggable>,
        allowOverwrite: Boolean = false
    ){
        if(allowOverwrite){
            dataHandlers[handlingClass] = dataHandler
        }else{
            dataHandlers.putIfAbsent(handlingClass, dataHandler)
        }
    }

    fun useHandler(
        dataHandler: LogHandler,
        allowOverwrite: Boolean = false
    ){
        if(allowOverwrite){
            dataHandlers[dataHandler.baseClassHandled] = dataHandler
        }else{
            dataHandlers.putIfAbsent(dataHandler.baseClassHandled, dataHandler)
        }
    }

    @Deprecated("useHandler")
    fun provideDataHandler(
        dataHandler: LogHandler,
        handlingClass: KClass<out StructuredLoggable>,
        allowOverwrite: Boolean = false
    ): Unit = useHandler(dataHandler, handlingClass, allowOverwrite)

    fun removeDataHandler(handlingClass: KClass<out StructuredLoggable>){
        dataHandlers.remove(handlingClass)
    }

    fun log(loggable: Loggable){
        val kClass = typeToken.kClass
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
                    warn("log", "ProceduralRecord branch hit")
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
                if(! handleUnStructured(loggable)){
                    if(debugMode){ outputDebud(DebugMethod.methodName("log2", "Impossible to process ${loggable::class}. Printing")) }
                    loggable.output()
                }
            }
        }
    }

    fun logData(data: T, noOutput: Boolean = false){
        if(handleStructured(data)){
            return
        }
        if(shouldStoreRecords){
            storeData(data)
        }
        onDataInterception?.invoke(data)

        if(!noOutput && !generalMute ){
            outputOrNot(data)
        }
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