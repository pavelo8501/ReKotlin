package po.misc.exceptions

import po.misc.data.printable.PrintableBase
import po.misc.data.printable.knowntypes.PropertyData
import po.misc.context.CTX
import po.misc.exceptions.models.ExceptionData

enum class HandlerType(val value: Int) {
    SkipSelf(1),
    CancelAll(2);

    companion object {
        fun fromValue(value: Int): HandlerType {
            return entries.firstOrNull { it.value == value } ?:CancelAll
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


    private var payloadBacking: ManagedCallSitePayload? = null
    internal val payload:  ManagedCallSitePayload? get() = payloadBacking

    val methodName: String? get() = payloadBacking?.methodName
    val context: CTX? get() = payloadBacking?.context

    open var handler: HandlerType = HandlerType.CancelAll
        internal set

    internal val exceptionDataBacking: MutableList<ExceptionData> = mutableListOf()
    val exceptionData: List<ExceptionData> = exceptionDataBacking


    constructor(managedPayload: ManagedCallSitePayload): this(managedPayload.message, managedPayload.code, managedPayload.cause){
        initFromPayload(managedPayload)
    }

    protected fun initFromPayload(payload: ManagedCallSitePayload) {
        this.payloadBacking = payload

        val  stackFrameMeta =  extractCallSiteMeta(payload.methodName, framesCount = 6)

        val data = ExceptionData(ExceptionEvent.Thrown, payload.message, payload.context)
        data.addStackTrace(currentCallerTrace(payload.methodName))
        exceptionDataBacking.add(data)
    }

    fun addExceptionData(
        data: ExceptionData,
        context: CTX
    ):ManagedException{
        val data = ExceptionData(ExceptionEvent.Thrown, msg, context)
        exceptionDataBacking.add(data)
        return this
    }

    fun setHandler(
        handlerType: HandlerType,
        producer: CTX,
    ): ManagedException {
        val thisMessage = message ?: ""
        if (exceptionData.isEmpty()) {
            val data = ExceptionData(ExceptionEvent.Thrown, thisMessage, producer)
            data.addStackTrace(stackTrace.toList())
            exceptionDataBacking.add(data)
        } else {
            val data =
                ExceptionData(ExceptionEvent.HandlerChanged, thisMessage, producer)
            exceptionDataBacking.add(data)
        }
        if (handler != handlerType) {
            handler = handlerType
        }
        return this
    }
    fun throwSelf(methodName: String, producer: CTX, event: ExceptionEvent = ExceptionEvent.Thrown):Nothing {
        val data = ExceptionData(event, message?:"", producer)
        data.addStackTrace(currentCallerTrace(methodName))
        exceptionDataBacking.add(data)
        throw this
    }

    fun setPropertySnapshot(snapshot: List<PropertyData>?):ManagedException{
        if(snapshot != null){
            val lastData = exceptionData.lastOrNull()
            lastData?.addPropertySnapshot(snapshot)
        }
        return this
    }

    companion object : ManageableException.Builder<ManagedException>{
        override fun build(message: String, source: Enum<*>?, original: Throwable?): ManagedException {
            return ManagedException(message, null, original)
        }
    }

}


