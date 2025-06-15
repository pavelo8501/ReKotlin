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
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.asIdentifiableClass
import po.misc.types.safeCast

abstract class TypedDataProcessorBase<T:PrintableBase<T>>(): IdentifiableClass{

    abstract val topEmitter: TypedDataProcessorBase<*>?

    override val identity = asIdentifiableClass("TypedDataProcessorBase")

    @PublishedApi
    internal val recordList: MutableList<T> = mutableListOf()
    private val activeRecord: T? get() = recordList.lastOrNull()
    val recordsCount : Int get() = recordList.size

    var globalMuteCondition:  ((ComposableData)-> Boolean)? = null
    var muteCondition: ((Printable)-> Boolean)? = null
    @PublishedApi
    internal var outputSource: ((String)-> Unit)? = null


    @PublishedApi
    internal fun addToProcessorList(record: T){
        recordList.add(record)
        onNewRecordCallback?.invoke(record)
    }

    private var onChildAddCallback : ((childRecord:PrintableBase<*>,parentRecord:T) -> Unit)? = null
    fun onChildAttached(callback: (childRecord:PrintableBase<*>, parentRecord:T) -> Unit){
        onChildAddCallback = callback
    }

    private var onNewRecordCallback: ((T) -> Unit)? = null
    fun onNewRecord(callback: (T) -> Unit){
        onNewRecordCallback = callback
    }

    fun addData(record: PrintableBase<*>){
        activeRecord?.let {
            it.addChild(record)
            onChildAddCallback?.invoke(record, it)
        }
    }

    fun provideOutputSource(source: (String)-> Unit){
        outputSource = source
    }

    fun  checkIfConditionApply(record: PrintableBase<T>){
        val casted = globalMuteCondition?.safeCast<(T)-> Boolean>()
        casted?.let {
            record.setMute(casted)
        }
    }

    fun processRecord(record: T, template: PrintableTemplateBase<T>?): String? {
        record.outputSource = outputSource
        addToProcessorList(record)
        checkIfConditionApply(record)
        globalMuteCondition?.let {
            record.setGenericMute(it)
        }
        return template?.let {
            record.printTemplate(it)
        } ?: run {
            record.print()
        }
    }

    @JvmName("processRecordPrintableBase")
    fun processRecord(record: PrintableBase<*>, template: PrintableTemplateBase<*>?): String? {
        record.outputSource = outputSource
        globalMuteCondition?.let {
            record.setGenericMute(it)
        }
        return record.print()
    }

    fun forwardTop(data: PrintableBase<*>){
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