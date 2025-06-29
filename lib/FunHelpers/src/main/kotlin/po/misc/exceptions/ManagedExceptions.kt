package po.misc.exceptions

import po.misc.interfaces.IdentifiableContext

enum class HandlerType(val value: Int) {
    Undefined(0),
    SkipSelf(1),
    CancelAll(2);
    companion object {
        fun fromValue(value: Int): HandlerType {
            entries.firstOrNull { it.value == value }?.let {
                return it
            }
            return Undefined
        }
    }
}

open class ManagedException(
    internal var msg: String,
    val source: Enum<*>? = null,
    original : Throwable? = null,
) : Throwable(msg, original), ManageableException<ManagedException>{

    enum class ExceptionEvent{
        Registered,
        HandlerChanged,
        Rethrown,
        Thrown
    }

    data class HandlingData(
       val wayPoint: IdentifiableContext,
       val event: ExceptionEvent,
       val message: String? = null
    ){
        override fun toString(): String {
            return when (event) {
                ExceptionEvent.Registered -> {
                    "First registered in ${wayPoint.contextName}"
                }
                ExceptionEvent.Thrown -> {
                    "Thrown in ${wayPoint.contextName}"
                }
                else -> {
                    "$wayPoint[${event.name}]"
                }
            }
        }
    }

    open var handler  : HandlerType = HandlerType.Undefined
        internal set
    override var propertySnapshot :  Map<String, Any?> = emptyMap()
    var handlingData: List<HandlingData> = listOf()
        internal set

    internal fun setMessage(message: String){
        msg = message
    }

    override fun addHandlingData(
        waypoint: IdentifiableContext,
        event: ExceptionEvent,
        message: String?
    ): ManagedException{
       handlingData = handlingData.toMutableList().apply { add(HandlingData(waypoint,event, message) ) }
       return this
    }
    override fun setHandler(
        handlerType: HandlerType,
        wayPoint: IdentifiableContext
    ): ManagedException{
        if(handlingData.isEmpty()){
            addHandlingData(wayPoint, ExceptionEvent.Registered)
        }else{
            addHandlingData(wayPoint, ExceptionEvent.HandlerChanged)
        }
        handler = handlerType
        return this
    }
    override fun throwSelf(wayPoint: IdentifiableContext):Nothing {
        addHandlingData(wayPoint, ExceptionEvent.Thrown)
        throw this
    }

    fun setPropertySnapshot(snapshot: Map<String, Any?>?):ManagedException{
        if(snapshot != null){
            propertySnapshot = snapshot
        }
        return this
    }

    companion object : ManageableException.Builder<ManagedException> {
        override fun build(message: String, source: Enum<*>?,  original: Throwable?): ManagedException {
            return ManagedException(message, null, original)
        }
    }

}


