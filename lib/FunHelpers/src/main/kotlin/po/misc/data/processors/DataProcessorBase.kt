package po.misc.data.processors

import po.misc.data.printable.Printable
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.PrintableTemplateBase
import po.misc.types.token.TypeToken


abstract class DataProcessorBase<T: Printable>(
    val topProcessor: DataProcessorBase<T>?,
    val hooks: ProcessorHooks<T>  = ProcessorHooks()
): DataProcessingHooks<T> by hooks{

    private val recordsBacking: MutableList<T> = mutableListOf()
    val records: List<T> get() = recordsBacking
    val recordsCount : Int get() = recordsBacking.size
    val activeRecord: T? get() = recordsBacking.lastOrNull()

    private val debugWhiteList: MutableMap<Int, TypeToken<*>> = mutableMapOf()

    init {
        topProcessor?.let {
            updateDebugWhiteList(it.debugWhiteList)
        }
    }

    private fun addSubProcessorsData(record: T, dataProcessor: DataProcessorBase<T>){
        hooks.subDataReceived?.invoke(record, dataProcessor)
        recordsBacking.add(record)
        topProcessor?.addSubProcessorsData(record, this)
    }

    fun addData(record: T){
        hooks.dataReceived?.invoke(record)
        recordsBacking.add(record)
        topProcessor?.addSubProcessorsData(record, this)
    }

    fun addArbitraryData(record: PrintableBase<*>){
        hooks.arbitraryDataReceived?.invoke(record)

        activeRecord?.let { active->
            if(active is PrintableBase<*>) {
                active.arbitraryMap.putPrintable(record)
            }
        }
    }

    protected fun updateDebugWhiteList(whiteList: MutableMap<Int, TypeToken<*>>){
        debugWhiteList.clear()
        debugWhiteList.putAll(whiteList)
    }

    fun clearData(){
        recordsBacking.clear()
    }

    fun <T: PrintableBase<T>> debugData(
        arbitraryRecord: T,
        printableClass: PrintableCompanion<T>,
        template: PrintableTemplateBase<T>?,
        debuggable:(T)-> Unit
    ){
        if(template != null){
            arbitraryRecord.setDefaultTemplate(template)
        }
        if(debugWhiteList.contains(printableClass.typeToken.hashCode())){
            debuggable.invoke(arbitraryRecord)
        }
    }
}

class DataProcessor<T: PrintableBase<T>>(
    topProcessor: DataProcessorBase<T>? = null,
):DataProcessorBase<T>(topProcessor){


}