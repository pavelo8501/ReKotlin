package po.misc.data.processors

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import po.misc.data.PrintableBase
import po.misc.data.console.PrintableTemplateBase
import po.misc.data.interfaces.ComposableData
import po.misc.data.interfaces.Printable
import po.misc.types.safeCast

abstract class DataProcessorBase<T:PrintableBase<T>>(){

    abstract val topEmitter: DataProcessorBase<*>?

    @PublishedApi
    internal val recordList: MutableList<PrintableBase<*>> = mutableListOf()
    private val activeRecord: PrintableBase<*>? get() = recordList.lastOrNull()
    val recordsCount : Int get() = recordList.size

    var globalMuteCondition:  ((ComposableData)-> Boolean)? = null
    var muteCondition: ((Printable)-> Boolean)? = null
    @PublishedApi
    internal var outputSource: ((String)-> Unit)? = null

    val hooks: ProcessorHooks<T>  = ProcessorHooks()

    private var onChildAddCallback : ((childRecord:PrintableBase<*>,parentRecord:T) -> Unit)? = null
    fun onChildAttached(callback: (childRecord:PrintableBase<*>, parentRecord:T) -> Unit){
        onChildAddCallback = callback
    }

    private fun addData(record: PrintableBase<*>){
        recordList.add(record)
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
        hooks.onData?.invoke(record)?:run {
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
            activeRecord?.addChild(data)?:run {
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

    fun forwardTop(data: PrintableBase<T>){
        topEmitter?.addData(data)
    }

    fun provideMuteCondition(muteCondition: (ComposableData)-> Boolean) {
        globalMuteCondition = muteCondition
    }

    @JvmName("provideMuteConditionTyped")
    fun <T: Printable> provideMuteCondition(muteCondition: (T)-> Boolean){
        this.muteCondition = muteCondition.safeCast<(Printable)-> Boolean>()
    }

    private val subscriberJobs = mutableListOf<Job>()

    private val notificationFlow = MutableSharedFlow<T>(
        replay = 10,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    private val notifications: SharedFlow<T> = notificationFlow.asSharedFlow()
    fun subscribeToDataEmissions(
        scope: CoroutineScope,
        collector: suspend (T) -> Unit
    ): Job {
        val job = scope.launch {
            notifications.collect(collector)
        }
        subscriberJobs += job
        return job
    }

    fun stopBroadcast() {
        subscriberJobs.forEach { it.cancel() }
        subscriberJobs.clear()
    }

    fun emitData(data: T) {
        CoroutineScope(Dispatchers.Default).launch {
            notificationFlow.emit(data)
        }
    }
}

class DataProcessor<T: PrintableBase<T>>(
    override val topEmitter: DataProcessorBase<*>? = null,
):DataProcessorBase<T>(){

}