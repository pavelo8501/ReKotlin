package po.misc.data.processors

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import po.misc.data.PrintableBase
import po.misc.data.console.PrintableTemplate
import po.misc.data.interfaces.ComposableData
import po.misc.data.interfaces.Printable
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.interfaces.asIdentifiable
import po.misc.registries.callback.TypedCallbackRegistry
import po.misc.types.safeCast

abstract class DataProcessorBase(){

    abstract val topEmitter: DataProcessorBase?

    private val selfAsIdentifiable: Identifiable = asIdentifiable("DataProcessorBase", "DataProcessorBase")

    private val builders : TypedCallbackRegistry<PrintableBase<*>, Unit> = TypedCallbackRegistry()

    var dataProvider: (()->PrintableBase<*>)? = null

    @PublishedApi
    internal val recordList: MutableList<PrintableBase<*>> = mutableListOf()
    val recordsCount : Int get() = recordList.size

    var globalMuteCondition:  ((ComposableData)-> Boolean)? = null
    var muteCondition: ((Printable)-> Boolean)? = null
    @PublishedApi
    internal var outputSource: ((String)-> Unit)? = null


    fun provideOutputSource(source: (String)-> Unit){
        outputSource = source
    }
    inline fun <reified T: Printable> checkIfConditionApply(record: PrintableBase<T>){
        val casted = globalMuteCondition?.safeCast<(T)-> Boolean>()
        casted?.let {
            record.setMute(casted)
        }
    }
    inline fun <reified T: Printable> processRecord(record: PrintableBase<T>, template: PrintableTemplate<T>?): String? {
        record.outputSource = outputSource
        recordList.add(record)
        checkIfConditionApply<T>(record)
        globalMuteCondition?.let {
            record.setGenericMute(it)
        }
        return template?.let {
            record.printTemplate(it)
        } ?: run {
            record.print()
        }
    }

    fun provideMuteCondition(muteCondition: (ComposableData)-> Boolean) {
        globalMuteCondition = muteCondition
    }

    @JvmName("provideMuteConditionTyped")
    fun <T: Printable> provideMuteCondition(muteCondition: (T)-> Boolean){
        this.muteCondition = muteCondition.safeCast<(Printable)-> Boolean>()
    }

    fun <T: Printable> registerEvent(event: ValueBased, dataBuilder: (PrintableBase<*>) -> Unit){
        builders.subscribe(selfAsIdentifiable, event, dataBuilder)
    }
    fun <T: Printable> raiseEvent(event: ValueBased, data:PrintableBase<*>){
       builders.trigger(selfAsIdentifiable, event, data)
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


