package po.misc.data.processors

import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.LogRecord
import po.misc.data.logging.Verbosity


abstract class DataProcessorBase2<T: LogRecord>(
    open val host: TraceableContext,
    val hooks: ProcessorHooks<T>  = ProcessorHooks()
): DataProcessingHooks<T> by hooks {

    private val recordsBacking: MutableList<T> = mutableListOf()

    private var onRecordInterception : ((T)-> Unit)? = null
    private var shouldStoreRecords = true

    val verbosity : Verbosity get() {
       return when(host){
            is Component -> (host as Component).componentID.verbosity
            else -> Verbosity.Info
        }
    }

    val records: List<T> get() = recordsBacking
    val activeRecord: T? get() = recordsBacking.lastOrNull()


    abstract fun outputOrNot(data: T)

    fun logData(data: T):T{
        onRecordInterception?.let {callback->
            callback.invoke(data)
            if(shouldStoreRecords){
                recordsBacking.add(data)
                outputOrNot(data)
            }
        }?:run {
            recordsBacking.add(data)
            outputOrNot(data)
        }
        return data
    }


    fun collectData(keepData: Boolean, callback:  (T)-> Unit){
        shouldStoreRecords = keepData
        onRecordInterception = callback
    }

    fun clear(){
        recordsBacking.clear()
    }

}


//
//abstract class DataProcessorBase<T: Printable>(
//    val topProcessor: DataProcessorBase<T>?,
//    val hooks: ProcessorHooks<T>  = ProcessorHooks()
//): DataProcessingHooks<T> by hooks{
//
//    private val recordsBacking: MutableList<T> = mutableListOf()
//    val records: List<T> get() = recordsBacking
//    val recordsCount : Int get() = recordsBacking.size
//    val activeRecord: T? get() = recordsBacking.lastOrNull()
//
//    private val debugWhiteList: MutableMap<Int, TypeToken<*>> = mutableMapOf()
//
//    init {
//        topProcessor?.let {
//            updateDebugWhiteList(it.debugWhiteList)
//        }
//    }
//
//    private fun addSubProcessorsData(record: T, dataProcessor: DataProcessorBase<T>){
//        hooks.subDataReceived?.invoke(record, dataProcessor)
//        recordsBacking.add(record)
//        topProcessor?.addSubProcessorsData(record, this)
//    }
//
//    fun addData(record: T){
//        hooks.dataReceived?.invoke(record)
//        recordsBacking.add(record)
//        topProcessor?.addSubProcessorsData(record, this)
//    }
//
//    fun addArbitraryData(record: PrintableBase<*>){
//        hooks.arbitraryDataReceived?.invoke(record)
//
//        activeRecord?.arbitraryMap?.putPrintable(record)
//    }
//
//    protected fun updateDebugWhiteList(whiteList: MutableMap<Int, TypeToken<*>>){
//        debugWhiteList.clear()
//        debugWhiteList.putAll(whiteList)
//    }
//
//    fun clearData(){
//        recordsBacking.clear()
//    }
//
//    fun <T: PrintableBase<T>> debugData(
//        arbitraryRecord: T,
//        printableClass: PrintableCompanion<T>,
//        template: PrintableTemplateBase<T>?,
//        debuggable:(T)-> Unit
//    ){
//        if(template != null){
//            arbitraryRecord.setDefaultTemplate(template)
//        }
//        if(debugWhiteList.contains(printableClass.typeKey.hashCode())){
//            debuggable.invoke(arbitraryRecord)
//        }
//    }
//}
