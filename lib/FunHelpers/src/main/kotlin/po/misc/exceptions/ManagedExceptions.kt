package po.misc.exceptions

import po.misc.data.printable.PrintableBase
import po.misc.data.printable.knowntypes.PropertyData
import po.misc.exceptions.models.ExceptionData
import po.misc.context.CTX
import po.misc.exceptions.models.ExceptionData2
import po.misc.types.currentCallerTrace


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
    open val msg: String,
    open val code: Enum<*>? = null,
    original : Throwable? = null
) : Throwable(msg, original), ManageableException<ManagedException>{

    enum class ExceptionEvent{
        Thrown,
        HandlerChanged,
        Rethrown,
        Executed
    }

    var payload: ManagedCallSitePayload? = null

    constructor(managedPayload: ManagedCallSitePayload) : this(managedPayload.message, managedPayload.code, managedPayload.cause){
        payload = managedPayload
    }

    open var handler: HandlerType = HandlerType.Undefined
        internal set

    var propertySnapshot : List<PropertyData> = emptyList()

    private var handlingDataBacking: MutableList<ExceptionData> = mutableListOf()
    val handlingData: List<ExceptionData> get() = handlingDataBacking

    internal val exceptionDataBacking: MutableList<ExceptionData2> = mutableListOf()
    val  exceptionData: List<ExceptionData2> = exceptionDataBacking

    init {

      // addExceptionData(payload?.toDataWithTrace(stackTrace.toList()) ?: ExceptionData2(ExceptionEvent.Thrown, msg, null))
    }

    private fun getExceptionDataByEvent(event:ExceptionEvent):ExceptionData?{
       return handlingData.firstOrNull { it.event == event }
    }

    fun createTrace(methodName: String): List<StackTraceElement>{
       val trace = currentCallerTrace(methodName)
       return trace
    }

    fun addExceptionData(
        data: ExceptionData2
    ):ManagedException{
        data.addStackTrace(this.stackTrace.toList())
        exceptionDataBacking.add(data)
        return  this
    }

    fun addExceptionData(
        context: CTX,
        callingMethodName: String
    ):ManagedException{
        val trace = currentCallerTrace(callingMethodName)
        val data = ExceptionData2(ExceptionEvent.Thrown, msg, context)
        data.addStackTrace(trace)
        exceptionDataBacking.add(data)
        return this
    }



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

        override fun build(message: String, source: Enum<*>?, original: Throwable?): ManagedException {
            return ManagedException(message, null, original)
        }
    }

}


