package po.api.ws_service.service.routing

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
import po.api.ws_service.WebSocketServer
import po.api.ws_service.service.models.ApiRequestDataType
import po.api.ws_service.service.models.WSApiRequest
import po.api.ws_service.service.models.WSApiResponse
import po.api.ws_service.services.Connection

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

    var receiveApiRequest : ((WSApiRequest<ApiRequestDataType>) -> Unit)? = null

    fun forwardResponse(request: WSApiRequest<ApiRequestDataType>){
        receiveApiRequest?.invoke(request)
        println("Forwarding response for ${request.module}")
    }
    fun sendApiResponse(response: WSApiResponse<*>){
        println("Sending response for ${response.request}")
        if(parent == null ) throw Exception("Parent not set`for method $method")
        parent?.responseReceived(response,typeInf)
    }
}

class ApiWebSocketClass(val path: String) {
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

    fun forwardRequest(request: WSApiRequest<ApiRequestDataType>){
        childApiMethods.find { it.method == request.module }.let {
            if(it != null){
                it.receiver?.invoke(it)
                it.forwardResponse(request)
            }else{
                WebSocketServer.apiLogger.warn("No method found for ${request.module}, request undelivered")
            }
        }
    }

    fun responseReceived(response:  WSApiResponse<*>, typeInfo: TypeInfo){
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
        val connection =  WebSocketServer.connectionService.addConnection(
            Connection(implementation.session, implementation.path, implementation)
        )
    }

    suspend fun webSocketHandler(session: DefaultWebSocketServerSession) {
        this.session = session
        active = true
        WebSocketServer.apiLogger.info("ApiWebSocketClass: New connection established on $path")
        registerConnection(this)
        WebSocketServer.apiLogger.info("New connection registered on $path")

        try {
            val request = session.receiveDeserialized<WSApiRequest<ApiRequestDataType>>()
            forwardRequest(request)
            while (active) {
                val request = session.receiveDeserialized<WSApiRequest<ApiRequestDataType>>()
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










