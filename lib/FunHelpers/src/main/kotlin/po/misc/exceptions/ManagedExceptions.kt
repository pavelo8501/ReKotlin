package po.misc.exceptions

import po.misc.data.printable.PrintableBase
import po.misc.data.printable.knowntypes.PropertyData
import po.misc.exceptions.models.ExceptionData
import po.misc.exceptions.models.ExceptionEventData
import po.misc.context.CTX
import po.misc.context.Identifiable
import po.misc.exceptions.models.ExceptionData2


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
    message: String,
    private val payload: ManagedCallSitePayload? = null,
    original : Throwable? = null,
) : Throwable(message, original), ManageableException<ManagedException>{

    enum class ExceptionEvent{
        Thrown,
        HandlerChanged,
        Rethrown,
        Executed
    }

    constructor(payload: ExceptionPayload) : this(payload.message, payload, payload.cause)

    open var handler: HandlerType = HandlerType.Undefined
        internal set

    var propertySnapshot : List<PropertyData> = emptyList()

    private var handlingDataBacking: MutableList<ExceptionData> = mutableListOf()
    val handlingData: List<ExceptionData> get() = handlingDataBacking

    internal val exceptionDataBacking: MutableList<ExceptionData2> = mutableListOf()
    val  exceptionData: List<ExceptionData2> = exceptionDataBacking

    init {
       addExceptionData(payload?.toDataWithTrace(stackTrace.toList()) ?: ExceptionData2(ExceptionEvent.Thrown, message, null))
    }

    private fun getExceptionDataByEvent(event:ExceptionEvent):ExceptionData?{
       return handlingData.firstOrNull { it.event == event }
    }


//    fun addBackTraceRecord(  record: PrintableBase<*>, producer: CTX){
//        println("handlingData count  ${handlingData.size}")
//        var done: Boolean = false
//        handlingData.forEach { data->
//            println("Events Items size: ${  data.events.items.size}")
//            data.events.items.forEach {
//                println("Event Name: ${it.event.name}")
//                println("backTraceRecords size: ${it.backTraceRecords.size}")
//                println("Producer: ${it.producer}")
//                println("StackTrace element: ${it.stackTraceElement}")
//            }
//        }
//      println(done)
//    }



    fun addExceptionData(
        data: ExceptionData2
    ):ManagedException{
        data.addStackTrace(this.stackTrace.toList())
        exceptionDataBacking.add(data)
        return  this
    }

//    fun addHandlingData(
//        data: ExceptionData
//    ): ManagedException{
//        byStackTrace(data)
//        handlingDataBacking.add(data)
//       return this
//    }

    fun setHandler(
        handlerType: HandlerType,
        producer: CTX,
        arbitraryData: PrintableBase<*>? = null
    ): ManagedException{
        val thisMessage = message?:""
        if(exceptionData.isEmpty()){
            val data =  ExceptionData2(ExceptionEvent.Thrown, thisMessage, producer).provideAuxData(arbitraryData)
            data.addStackTrace(stackTrace.toList())
            exceptionDataBacking.add(data)
        }else{
            val data = ExceptionData2(ExceptionEvent.HandlerChanged, thisMessage, producer).provideAuxData(arbitraryData)
            exceptionDataBacking.add(data)
        }
        if(handler != handlerType){
            handler = handlerType
        }
        return this
    }


    fun throwSelf(producer: CTX,  event: ExceptionEvent = ExceptionEvent.Thrown):Nothing {
        val data = ExceptionData2(event, message?:"", producer)
        data.addStackTrace(stackTrace.toList())
        addExceptionData(data)
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
            return ManagedException(payload.message, payload, payload.cause)
        }

        override fun build(message: String, source: Enum<*>?,  original: Throwable?): ManagedException {
            return ManagedException(message, null, original)
        }


    }

}


