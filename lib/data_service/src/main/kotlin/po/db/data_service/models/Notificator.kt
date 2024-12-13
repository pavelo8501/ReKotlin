package po.db.data_service.models

import po.db.data_service.dto.CanNotify

data class NotificationCallbacks(
    val onStart : (()-> Unit)? = null,
    val onFinish : (()-> Unit)? = null,
    val onInitialized : (()-> Unit)? = null,
    val onComplete : (()-> Unit)? = null
){
    fun asArray():Array<()->Unit>{
        return Array<()->Unit>(4) {
            {
                onStart
                onFinish
                onInitialized
                onComplete
            }
        }
    }
}

data class NotifySubscription(
   private val name: String,
   private val notification : ()->Unit,
   private val callbackFun : ()->Unit
){
    fun triggerNotification(){
        callbackFun()
    }
}

class Notificator(private val owner : CanNotify) {

    val notifications = NotificationCallbacks().asArray()
    val subscriptions = mutableMapOf<String, NotifySubscription>()

    inline fun <reified T: Any>subscribe(subscriber:T, noinline notification : ()->Unit, noinline callbackFun : ()->Unit ){
      val name =   subscriber::class.qualifiedName?:subscriber::class.simpleName?: throw Exception("Unable read name")
      this.subscriptions.putIfAbsent( name ,NotifySubscription(name, notification, callbackFun ) ).let {
          if(it!= null){
              //Logic to add subscription
          }
      }
    }

    fun triggerNotification(){
        this.subscriptions.forEach { it.value.triggerNotification() }
    }

}