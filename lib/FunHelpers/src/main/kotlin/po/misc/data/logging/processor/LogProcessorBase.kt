package po.misc.data.logging.processor

import po.misc.context.component.Component
import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.Verbosity
import po.misc.data.printable.Printable
import po.misc.data.processors.DataProcessingHooks
import po.misc.data.processors.ProcessorHooks

abstract class LogProcessorBase<T: Printable>(
    open val host: TraceableContext,
    val hooks: ProcessorHooks<T> = ProcessorHooks()
): DataProcessingHooks<T> by hooks {

    private val recordsBacking: MutableList<T> = mutableListOf()

    private var onRecordInterception : ((T)-> Unit)? = null
    private var shouldStoreRecords = true

    val verbosity : Verbosity
        get() {
       return when(host){
            is Component -> (host as Component).componentID.verbosity
            else -> Verbosity.Info
        }
    }

    val records: List<T> get() = recordsBacking
    val activeRecord: T? get() = recordsBacking.lastOrNull()

    abstract fun outputOrNot(data: T)

    fun logData(data: T, noOutput: Boolean = false):T{
        onRecordInterception?.let {callback->
            callback.invoke(data)
            if(shouldStoreRecords){
                recordsBacking.add(data)
            }
        }?:run {
            recordsBacking.add(data)
            if(!noOutput){
                outputOrNot(data)
            }
        }
        return data
    }

    fun collectData(keepData: Boolean, callback: (T)-> Unit){
        shouldStoreRecords = keepData
        onRecordInterception = callback
    }

    fun dropCollector(){
        onRecordInterception = null
        shouldStoreRecords = true
    }


    fun clear(){
        recordsBacking.clear()
    }
}