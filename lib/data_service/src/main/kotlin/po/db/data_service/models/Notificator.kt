package po.db.data_service.models

import po.db.data_service.dto.CanNotify

enum class NotificationEvent{
    ON_START,
    ON_STOP,
    ON_INITIALIZED,
    ON_ERROR;
    companion object {
        fun isValidEvent(name: String): Boolean = entries.any { it.name == name }
    }
}

data class NotifySubscription(
   private val name: String,
   private val callbackFun : ()->Unit
){
    fun triggerCallback(){
        this.callbackFun.invoke()
    }
}

class Notificator(private val owner : CanNotify) {

    private val subscriptions = mutableMapOf<String, MutableList<NotifySubscription>>()

    private fun addSubscription(subscriber: String, event: NotificationEvent, callback: () -> Unit) {
        subscriptions.computeIfAbsent(event.name) { mutableListOf() }
            .add(NotifySubscription(subscriber,  callback))
    }

    fun trigger(event: NotificationEvent) {
        subscriptions[event.name]?.forEach { subscription ->
            subscription.triggerCallback()
        }
    }

    inline fun <reified T: CanNotify>subscribe(event : NotificationEvent, noinline callbackFun : ()->Unit ){
      val name = T::class.qualifiedName?:T::class.simpleName?: throw Exception("Can not infer class name of a subscriber")
      this.subscribe(name, event, callbackFun)
    }

    fun subscribe(subscriberName:String,  event : NotificationEvent, callbackFun : ()->Unit ){
        this.addSubscription(subscriberName, event, callbackFun)
    }


}