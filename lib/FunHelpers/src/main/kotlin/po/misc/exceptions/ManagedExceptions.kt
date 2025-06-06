package po.misc.exceptions

import po.misc.interfaces.Identifiable
import po.misc.interfaces.asIdentifiable
import po.misc.types.safeCast
import kotlin.reflect.full.companionObjectInstance

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

sealed interface SelfThrownException<E:ManagedException>  {
    val message: String
    //var handler  : HandlerType
    var propertySnapshot :  Map<String, Any?>

    fun setHandler(handlerType: HandlerType,  wayPoint: Identifiable? = null): E
    fun throwSelf(wayPoint: Identifiable? = null): Nothing

    interface Builder<E> {
        fun build(message: String, optionalCode: Int?, original : Throwable?): E
    }

    companion object {
        inline fun <reified E : ManagedException> build(message: String, optionalCode: Int?, original : Throwable? = null): E {
            return E::class.companionObjectInstance?.safeCast<Builder<E>>()?.build(message, optionalCode, original)
                ?: throw IllegalStateException("Companion object must implement Builder<E> @ SelfThrownException")
        }

    }
}

open class ManagedException(
    override var message: String,
    original : Throwable? = null
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

    internal val selfIdentifiable : Identifiable = asIdentifiable(message, "ManagedException")

    open var handler  : HandlerType = HandlerType.SKIP_SELF
        internal set
    override var propertySnapshot :  Map<String, Any?> = emptyMap()
    var handlingData: List<HandlingData> = listOf()
        internal set

    fun addHandlingData(
        waypoint: Identifiable,
        event: ExceptionEvent,
        message: String? = null
    ): ManagedException{
       handlingData = handlingData.toMutableList().apply { add(HandlingData(waypoint,event, message) ) }
       return this
    }
    override fun setHandler(
        handlerType: HandlerType,
        wayPoint: Identifiable?
    ): ManagedException{
        addHandlingData(wayPoint?:selfIdentifiable, ExceptionEvent.HandlerChanged)
        handler = handlerType
        return this
    }
    fun setPropertySnapshot(snapshot: Map<String, Any?>?):ManagedException{
        if(snapshot != null){
            propertySnapshot = snapshot
        }
        return this
    }

    override fun throwSelf(wayPoint: Identifiable?):Nothing {
        addHandlingData(wayPoint?:selfIdentifiable, ExceptionEvent.Thrown)
        throw this
    }
}


