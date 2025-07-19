package po.misc.exceptions

import po.misc.data.printable.PrintableBase
import po.misc.data.printable.knowntypes.PropertyData
import po.misc.exceptions.models.ExceptionData
import po.misc.exceptions.models.ExceptionEventData
import po.misc.context.CTX
import po.misc.context.Identifiable


enum class HandlerType(val value: Int) {
    Undefined(0),
    SkipSelf(1),
    CancelAll(2);

    companion object {
        fun fromValue(value: Int): HandlerType {
            return entries.firstOrNull { it.value == value } ?:Undefined
        }
    }
}

open class ManagedException(
    internal var msg: String,
    val source: Enum<*>? = null,
    original : Throwable? = null,
) : Throwable(msg, original), ManageableException<ManagedException>{

    enum class ExceptionEvent{
        Thrown,
        HandlerChanged,
        Rethrown,
        Executed
    }

    open var handler: HandlerType = HandlerType.Undefined
        internal set

    var propertySnapshot :  List<PropertyData> = emptyList()

    private var handlingDataBacking: MutableList<ExceptionData> = mutableListOf()
    val handlingData: List<ExceptionData> get() = handlingDataBacking.toList()

    private fun byStackTrace(data: ExceptionData){
        data.addStackTraceElement(stackTrace.toList())

    }

    private fun getExceptionDataByEvent(event:ExceptionEvent):ExceptionData?{
       return handlingData.firstOrNull { it.event == event }
    }

    internal fun setMessage(message: String){
        msg = message
    }


    fun addBackTraceRecord(record: PrintableBase<*>, producer: CTX){

        println("handlingData count  ${handlingData.size}")


        var done: Boolean = false
        handlingData.forEach { data->
            println("Events Items size: ${  data.events.items.size}")
            data.events.items.forEach {
                println("Event Name: ${it.event.name}")
                println("backTraceRecords size: ${it.backTraceRecords.size}")
                println("Producer: ${it.producer}")
                println("StackTrace element: ${it.stackTraceElement}")
            }
        }
      println(done)
    }

    fun addHandlingData(
        data: ExceptionData
    ): ManagedException{
        byStackTrace(data)
        handlingDataBacking.add(data)
       return this
    }

    fun setHandler(
        handlerType: HandlerType,
        producer: CTX
    ): ManagedException{
        if(handlingData.isEmpty()){
          val data =  ExceptionData.createThrown(producer)
            addHandlingData(data)

        }else{
            addHandlingData(ExceptionData.createThrown(producer))
        }
        if(handler != handlerType){
            handler = handlerType
        }
        return this
    }


    fun throwSelf(producer: CTX,  event: ExceptionEvent = ManagedException.ExceptionEvent.Thrown):Nothing {
        getExceptionDataByEvent(event)?.let {
            val eventData = ExceptionEventData(it, producer.completeName, producer::class.qualifiedName.toString(), event.name)
            it.events.addItem(eventData)
        }?:run {
            val data = ExceptionData.fromExceptionEvent(event, producer)
            addHandlingData(data)
        }
        throw this
    }

    fun setPropertySnapshot(snapshot: List<PropertyData>?):ManagedException{
        if(snapshot != null){
            propertySnapshot = snapshot
        }
        return this
    }

    companion object : ManageableException.Builder<ManagedException>{


        fun create(payload: ManagedCallSitePayload): ManagedException{
            return ManagedException(payload.message, payload.source, payload.cause)
        }

        override fun build(message: String, source: Enum<*>?,  original: Throwable?): ManagedException {
            return ManagedException(message, null, original)
        }


    }

}


