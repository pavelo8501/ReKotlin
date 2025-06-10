package po.misc.data.processors

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

abstract class TypedDataProcessorBase<T:PrintableBase<T>>(){

    abstract val topEmitter: TypedDataProcessorBase<*>?

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


    private val notificationFlow = MutableSharedFlow<PrintableBase<*>>(
        replay = 10,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    val notifications: SharedFlow<PrintableBase<*>> = notificationFlow.asSharedFlow()

    fun emitData(notification: PrintableBase<*>) {
        CoroutineScope(Dispatchers.Default).launch {
            notificationFlow.emit(notification)
        }
    }
}