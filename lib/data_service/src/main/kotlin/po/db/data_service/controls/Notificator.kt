package po.db.data_service.controls

import po.db.data_service.dto.interfaces.CanNotify
import po.db.data_service.dto.interfaces.DAOWInstance

enum class NotificationEvent{
    ON_START,
    ON_STOP,
    ON_INITIALIZED,
    ON_ERROR;
    companion object {
        fun isValidEvent(name: String): Boolean = entries.any { it.name == name }
    }
}

data class NotifySubscription<T : Any?>(
    private val name: String,
    val callbackFun: (T?)->Unit
){

}

class Notificator(private val owner : CanNotify) {

    private val subscriptions = mutableMapOf<String, MutableList<NotifySubscription<*>>>()

    private fun <T:Any>addSubscription(subscriber: String, event: NotificationEvent, callback: (T?) -> Unit) {
        subscriptions.computeIfAbsent(event.name) { mutableListOf() }
            .add(NotifySubscription(subscriber,  callback ))
    }

    fun <T:Any?>trigger(event: NotificationEvent, optionalReceiver: T? = null ) {
        subscriptions[event.name]?.forEach { subscription ->
            (subscription as? NotifySubscription<T>)?.callbackFun?.let { it(optionalReceiver) }
        }
    }

    fun <T:Any?>subscribe(subscriberName:String, event : NotificationEvent, callbackFun : (T?)->Unit ){
        this.addSubscription(subscriberName, event, callbackFun)
    }
}

inline fun <reified S: DAOWInstance, H : CanNotify, T:Any?> CanNotify.subscribe(event : NotificationEvent, noinline callbackFun : (T?)->Unit ){
    val name = S::class.qualifiedName?:S::class.simpleName?: throw Exception("Can not infer class name of a subscriber")
    this.notificator.subscribe(name,event,callbackFun)

}
