package po.misc.data.processors


import po.misc.collections.StaticTypeKey
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.data.console.PrintableTemplateBase
import po.misc.data.printable.ComposableData
import po.misc.data.printable.Printable
import po.misc.types.safeCast

abstract class DataProcessorBase<T:PrintableBase<T>>(
    val topEmitter: DataProcessorBase<*>?,
    val emitter: EmittableFlow<T>?
)
{
    @PublishedApi
    internal val recordList: MutableList<PrintableBase<*>> = mutableListOf()
    private val activeRecord: PrintableBase<*>? get() = recordList.lastOrNull()
    val recordsCount : Int get() = recordList.size

    var globalMuteCondition:  ((ComposableData)-> Boolean)? = null
    var muteCondition: ((Printable)-> Boolean)? = null
    @PublishedApi
    internal var outputSource: ((String)-> Unit)? = null

    val hooks: ProcessorHooks<T>  = ProcessorHooks()
    private val debugWhiteList: MutableMap<Int, StaticTypeKey<*>> = mutableMapOf()

    init {
        topEmitter?.hooks?.debugListUpdated{ topEmittersList->
            updateDebugWhiteList(topEmittersList)
        }
    }

    private fun addData(record: PrintableBase<*>){
        recordList.add(record)
    }
    private fun acceptForwarded(record:  PrintableBase<*>){
        activeRecord?.let {
            it.addChild(record)
            hooks.onChildAttached?.invoke(record, it)
        }?:run {
            recordList.add(record)
        }
    }

    protected fun updateDebugWhiteList(whiteList: MutableMap<Int, StaticTypeKey<*>>){
        debugWhiteList.clear()
        debugWhiteList.putAll(whiteList)
        hooks.onDebugListUpdated?.invoke(debugWhiteList)
    }

    fun allowDebug(vararg printableClass: PrintableCompanion<*>){
        printableClass.forEach {
            debugWhiteList[it.typeKey.hashCode()] = it.typeKey
        }
        hooks.onDebugListUpdated?.invoke(debugWhiteList)
    }

    fun <T: PrintableBase<T>> debugData(arbitraryRecord: T, printableClass: PrintableCompanion<T>, template: PrintableTemplateBase<T>?, debuggable:(T)-> Unit){
        if(template != null){
            arbitraryRecord.defaultTemplate = template
        }
        if(debugWhiteList.contains(printableClass.typeKey.hashCode())){
            debuggable.invoke(arbitraryRecord)
        }
    }

    fun processRecord(record: T, template: PrintableTemplateBase<T>?){
        if(template != null){
            record.changeDefaultTemplate(template)
        }
        record.outputSource = outputSource
        recordList.add(record)
        checkIfConditionApply(record)
        globalMuteCondition?.let {
            record.setGenericMute(it)
        }
        hooks.onDataReceived?.invoke(record)?:run {
            record.echo()
        }
    }

    /**
     * Logs provided record as a child data record applying template: PrintableTemplateBase<T2> as it's default
     * If onArbitraryData hook is not registered data is applied to the active item as child record if it exists
     * if not arbitrary data is being added to the list effectively becoming an active item
     */
    fun <T2: PrintableBase<T2>> logData(data:T2, template: PrintableTemplateBase<T2>):T2{
        data.changeDefaultTemplate(template)
        hooks.onArbitraryData?.invoke(data) ?:run {
            activeRecord?.let {
                it.addChild(data)
                hooks.onChildAttached?.invoke(data, it)
            }?:run {
                addData(data)
            }
            data.echo()
        }
        return data
    }

    fun provideOutputSource(source: (String)-> Unit){
        outputSource = source
    }

    fun checkIfConditionApply(record: PrintableBase<T>){
        val casted = globalMuteCondition?.safeCast<(T)-> Boolean>()
        casted?.let {
            record.setMute(casted)
        }
    }

    fun forwardOrEmmit(data: T){
        topEmitter?.acceptForwarded(data)
        emitter?.emitData(data)
    }

    fun provideMuteCondition(muteCondition: (ComposableData)-> Boolean) {
        globalMuteCondition = muteCondition
    }

    @JvmName("provideMuteConditionTyped")
    fun <T: Printable> provideMuteCondition(muteCondition: (T)-> Boolean){
        this.muteCondition = muteCondition.safeCast<(Printable)-> Boolean>()
    }

}

class DataProcessor<T: PrintableBase<T>>(
    topEmitter: DataProcessorBase<*>?,
    emitter: EmittableFlow<T>? = null
):DataProcessorBase<T>(topEmitter, emitter){

}