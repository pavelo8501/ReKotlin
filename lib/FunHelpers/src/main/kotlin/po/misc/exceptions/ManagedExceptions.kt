package po.misc.exceptions

import po.misc.interfaces.Identifiable

enum class HandlerType(val value: Int) {
    GENERIC(0),
    SKIP_SELF(1),
    CANCEL_ALL(2),
    UNMANAGED(3);
    companion object {
        fun fromValue(value: Int): HandlerType {
            entries.firstOrNull { it.value == value }?.let {
                return it
            }
            return GENERIC
        }
    }
}


open class ManagedException(
    message: String,
    val  source: Enum<*>? = null,
    original : Throwable? = null,
) : Throwable(message, original), SelfThrownException<ManagedException>{

    enum class ExceptionEvent{
        Registered,
        HandlerChanged,
        Rethrown,
        Thrown
    }

    data class HandlingData(
       val wayPoint: Identifiable,
       val event: ExceptionEvent,
       val message: String? = null
    )

    open var handler  : HandlerType = HandlerType.SKIP_SELF
        internal set
    override var propertySnapshot :  Map<String, Any?> = emptyMap()
    var handlingData: List<HandlingData> = listOf()
        internal set

    override fun addHandlingData(
        waypoint: Identifiable,
        event: ExceptionEvent,
        message: String?
    ): ManagedException{
       handlingData = handlingData.toMutableList().apply { add(HandlingData(waypoint,event, message) ) }
       return this
    }
    override fun setHandler(
        handlerType: HandlerType,
        wayPoint: Identifiable
    ): ManagedException{
        addHandlingData(wayPoint, ExceptionEvent.HandlerChanged)
        handler = handlerType
        return this
    }
    fun setPropertySnapshot(snapshot: Map<String, Any?>?):ManagedException{
        if(snapshot != null){
            propertySnapshot = snapshot
        }
        return this
    }

    override fun throwSelf(wayPoint: Identifiable):Nothing {
        addHandlingData(wayPoint, ExceptionEvent.Thrown)
        throw this
    }


    companion object : SelfThrownException.Builder<ManagedException> {
        override fun build(message: String, source: Enum<*>?,  original: Throwable?): ManagedException {
            return ManagedException(message, null, original)
        }
    }

}


