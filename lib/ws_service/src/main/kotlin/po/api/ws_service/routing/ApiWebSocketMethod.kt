package po.api.ws_service.service.routing

import io.ktor.server.websocket.DefaultWebSocketServerSession


interface WebSocketMethodObserver {
    fun onMethodInvoked(module: String, call: ApiWebSocketMethodContext.() -> Unit)
}

class ApiWebSocketMethodContext{
    var receiveApiRequest : ((String) -> Unit)? = null

    inline fun <reified T> ApiWebSocketMethodContext.receiveApiRequest(response: T){}

    fun sendApiRequest(response: String){
        println("response")
        println(response)
    }
}

data class WebSocketMethod(
    val method: String,
    val call: ApiWebSocketMethodContext.() -> Unit
)

object ApiWebSocketMethodClass : WebSocketMethodObserver{
    fun apiWebSocketMethodClassTest(){
        println("apiWebSocketMethodClassTest")
    }
    private val listeners = mutableListOf<WebSocketMethodObserver>()





    fun registerListener(listener: WebSocketMethodObserver) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: WebSocketMethodObserver) {
        listeners.remove(listener)
    }

    override fun onMethodInvoked(module: String, call: ApiWebSocketMethodContext.() -> Unit) {

        listeners.forEach {
            it.onMethodInvoked(module, call)
        }
    }
}


fun DefaultWebSocketServerSession.apiWebSocketMethod(
    module: String,
    methodReceiver:ApiWebSocketMethodContext.() -> Unit)
{
    val apiWebSocketMethodClass : ApiWebSocketMethodClass = ApiWebSocketMethodClass

    ApiWebSocketMethodClass.onMethodInvoked(module,methodReceiver)
    methodReceiver.invoke(ApiWebSocketMethodContext())

    //var serializer: KSerializer<T>? = null

}

