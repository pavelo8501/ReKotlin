package po.wswraptor.routing

import po.wswraptor.WebSocketServer
import po.wswraptor.models.response.WSResponse
import po.wswraptor.services.Connection
import io.ktor.server.routing.Routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import po.wswraptor.models.request.WSRequest


fun  Routing.apiWebSocket(
    path: String,
    receiver: ApiWebSocketClass.() -> Unit) {

    webSocket(path) {
        println("webSocket: New connection established on $path")
        val apiWebSocket = ApiWebSocketClass(path)
        apiWebSocket.receiver()
        apiWebSocket.webSocketHandler(this)
    }
}

class ClassContextHolder(
    val clazz : KSerializer<*>,
    val serializer: KSerializer<ClassContextHolder>
)


class ApiWebSocketMethodClass(val method: String, val typeInf : TypeInfo) {

    var path: String = ""
    var parent: ApiWebSocketClass? = null
    var receiver: (ApiWebSocketMethodClass.() -> Unit)? = null

    var receiveApiRequest : ((WSRequest<Any>) -> Unit)? = null

    fun forwardResponse(request: WSRequest<Any>){
        receiveApiRequest?.invoke(request)
        println("Forwarding response for ${request.resource}")
    }
    fun sendApiResponse(response: WSResponse<*>){
        println("Sending response for ${response.resource}")
        if(parent == null ) throw Exception("Parent not set`for method $method")
        parent?.responseReceived(response,typeInf)
    }
}

class ApiWebSocketClass(val path: String, val server : WebSocketServer? = null) {
    lateinit var  session : DefaultWebSocketServerSession

    var active: Boolean = false

    val webSocketClassScope = CoroutineScope(Job() + Dispatchers.Default + CoroutineName("ApiWebSocketClass  Coroutine"))

    private val childApiMethods = mutableListOf<ApiWebSocketMethodClass>()

    fun getApiMethod(method: String): ApiWebSocketMethodClass? {
        return  childApiMethods.firstOrNull { it.method == method  }
    }
    fun getApiMethods(): List<ApiWebSocketMethodClass> {
        return  childApiMethods.toList()
    }

    fun forwardRequest(request: WSRequest<Any>){
        childApiMethods.find { it.method == request.resource }.let {
            if(it != null){
                it.receiver?.invoke(it)
                it.forwardResponse(request)
            }else{
               // WebSocketServer.apiLogger.warn("No method found for ${request.module}, request undelivered")
            }
        }
    }

    fun responseReceived(response:  WSResponse<*>, typeInfo: TypeInfo){
        webSocketClassScope.launch {
            session.sendSerialized(response, typeInfo)
        }
    }

    fun addWebSocketMethod(apiMethod: ApiWebSocketMethodClass){
        apiMethod.path = path
        apiMethod.parent = this
        childApiMethods.add(apiMethod)
    }

    inline fun <reified  T> apiWebSocketMethod(method: String, protocol: String? = null, noinline receiver: ApiWebSocketMethodClass.() -> Unit) {
        val typeInfo = typeInfo<T>()
        if(typeInfo.kotlinType == null) {
            throw Exception("TypeInfo does not contain a valid KClass")
        }
        val apiMethod = ApiWebSocketMethodClass(method.lowercase(), typeInfo).also {
            it.receiver = receiver
        }
        addWebSocketMethod(apiMethod)
    }

    fun registerConnection(implementation :ApiWebSocketClass){
        val connection =  server!!.connectionService.addConnection(
            Connection(implementation.session, implementation.path, implementation)
        )
    }

    suspend fun webSocketHandler(session: DefaultWebSocketServerSession) {
        this.session = session
        active = true
     //   WebSocketServer.apiLogger.info("ApiWebSocketClass: New connection established on $path")
        registerConnection(this)
      //  WebSocketServer.apiLogger.info("New connection registered on $path")

        try {
            val request = session.receiveDeserialized<WSRequest<Any>>()
            forwardRequest(request)
            while (active) {
                val request = session.receiveDeserialized<WSRequest<Any>>()
                webSocketClassScope.launch {
                    forwardRequest(request)
                }
            }
        }catch (e: ClosedReceiveChannelException) {
            println("onClose ${session.closeReason.await()}")
        } catch (e: Throwable) {
            println("onError ${session.closeReason.await()}")
            e.printStackTrace()
        }
    }
}










