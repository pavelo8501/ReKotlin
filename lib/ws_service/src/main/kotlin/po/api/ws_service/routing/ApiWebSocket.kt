package po.api.ws_service.service.routing

import io.ktor.server.routing.Routing
import io.ktor.server.routing.RoutingRoot
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.webSocket
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import po.api.ws_service.WebSocketServer
import po.api.ws_service.service.models.ApiRequestDataType
import po.api.ws_service.service.models.WSApiRequest
import po.api.ws_service.service.models.WSApiRequestDataInterface
import po.api.ws_service.services.Connection
import kotlin.reflect.KClass
import kotlin.reflect.KType

fun  Routing.apiWebSocket(
    path: String,
    protocol: String?=null,
    receiver: ApiWebSocketClass.() -> Unit) {

    webSocket(path, protocol) {
        println("webSocket: New connection established on $path")
        val apiWebSocket = ApiWebSocketClass(path,protocol,this)
        apiWebSocket.receiver()
        val request = receiveDeserialized<WSApiRequest<ApiRequestDataType>>()
        apiWebSocket.forwardRequest(request)
        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {}
                    else -> {}
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            println("onClose ${closeReason.await()}")
        } catch (e: Throwable) {
            println("onError ${closeReason.await()}")
            e.printStackTrace()
        }
    }
}


class ClassContextHolder(
    val clazz : KSerializer<*>,
    val serializer: KSerializer<ClassContextHolder>
)

class ApiWebSocketMethodClass(val method: String, val protocol: String?, val typeInfo:  KType) {
    companion object{
        fun onMethodInvoked(
            module: String,
            classContainer: ClassContextHolder,
            call: ApiWebSocketMethodClass.() -> Unit
        ) {
            println("onMethodInvoked in $module")
        }
    }

    var path: String = ""
    var receiver: (ApiWebSocketMethodClass.() -> Unit)? = null

    var receiveApiRequest : ((WSApiRequest<*>) -> Unit)? = null
    fun <T : ApiRequestDataType>forwardResponse(request: WSApiRequest<T>){
        receiveApiRequest?.invoke(request)
    }
}



class ApiWebSocketClass(val path: String, val protocol: String?, val session: DefaultWebSocketServerSession) {
    companion object{
       fun registerConnection(implementation :ApiWebSocketClass){
            val connection =  WebSocketServer.connectionService.addConnection(
                Connection(implementation.session, implementation.path, implementation.protocol,implementation)
            )
        }
        fun registerMethodForConnection(wsMethod : ApiWebSocketMethodClass, session: WebSocketServerSession){
          //  WebSocketServer.connectionService.addWebSocketMethod()
        }
    }

    val apiMethods = mutableListOf<ApiWebSocketMethodClass>()

    suspend fun forwardRequest(request: WSApiRequest<ApiRequestDataType>){
        apiMethods.find { it.method == request.module }.let {
            if(it != null){
                it.forwardResponse(request)
            }else{
                WebSocketServer.apiLogger.warn("No method found for ${request.module}, request undelivered")
            }
        }
    }

    fun bindToSharedFlow(flow: MutableSharedFlow<WSApiRequest<ApiRequestDataType>>){
//        flow.collect{
//            println("Received from Shared flow on $path  ${it.module}")
//        }
    }

    fun addWebSocketMethod(apiMethod: ApiWebSocketMethodClass){
        apiMethod.path = path
       // registerMethodForConnection(apiMethod, session)
        apiMethods.add(apiMethod)
    }

    inline fun <reified  T> apiWebSocketMethod(method: String, protocol: String? = null, noinline receiver: ApiWebSocketMethodClass.() -> Unit) {

            val typeInfo = typeInfo<T>()
            val kotlinType = typeInfo.kotlinType
            if(kotlinType == null) {
                throw Exception("TypeInfo does not contain a valid KClass")
            }
            val apiMethod = ApiWebSocketMethodClass(method.lowercase(), protocol?.lowercase(), kotlinType).also {
                it.receiver = receiver
            }
            addWebSocketMethod(apiMethod)
    }


    init {
        WebSocketServer.apiLogger.info("ApiWebSocketClass: New connection established on $path")
        registerConnection(this)
        WebSocketServer.apiLogger.info("New connection registered on $path")
    }

}










