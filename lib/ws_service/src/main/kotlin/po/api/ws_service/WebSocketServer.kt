package po.api.ws_service

import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.json.Json


import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.modules.polymorphic
import po.api.rest_service.*
import po.api.rest_service.server.ApiConfig
import po.api.ws_service.models.CreateRequestData
import po.api.ws_service.models.DeleteRequestData
import po.api.ws_service.models.RequestData
import po.api.ws_service.models.SelectRequestData
import po.api.ws_service.models.WSApiRequest
import po.api.ws_service.models.WSApiResponse
import po.api.ws_service.plugins.PolymorphicJsonConverter
import po.api.ws_service.plugins.sendCloseReason
import kotlin.time.Duration

import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.ApplicationCall
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingRoot
import io.ktor.server.routing.method
import io.ktor.server.routing.route
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.util.AttributeKey
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import po.api.rest_service.apiLogger
import po.api.ws_service.classes.ApiWebSocketHelper
import po.api.ws_service.extensions.RouteContent
import po.api.ws_service.extensions.RouteNegotiator
import po.api.ws_service.plugins.ApiWebSockets
import po.api.ws_service.security.ActiveUsers
import po.api.ws_service.security.ApiUser


val CallAttributeKey = AttributeKey<ApplicationCall>("call")
class WebSocketMethodRegistryItem(var  method: String, body: suspend WebSocketMethodDataContext.() -> Unit)

class WebSocketMethodDataContext(){
    val receiveApiRequest : (suspend (Unit) -> Unit)? = null

    suspend fun sendApiRequest(){

    }
}

val webSocketMethodRegistryKey = AttributeKey<MutableList<WebSocketMethodRegistryItem>>("WebSocketMethod")

inline fun <reified T : Any> DefaultWebSocketServerSession.apiWebSocketMethod(
    module: String,
    noinline body: suspend WebSocketMethodDataContext.() -> Unit){

    val serializer = serializer<T>()
    val methodRegistry = call.attributes.getOrNull(webSocketMethodRegistryKey) ?: mutableListOf()
    methodRegistry.add(WebSocketMethodRegistryItem(module,body))
    call.attributes.put(webSocketMethodRegistryKey, methodRegistry)
}


 fun Route.apiWebSocket(
    path: String,
    protocol: String? = null,
    handler: suspend DefaultWebSocketServerSession.() -> Unit){

     webSocket(path, protocol) {
         println("apiWebSocket: New connection established on $path")
       /*  if (!call.request.headers.contains("X-My-Auth-Token")) {
             close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Unauthorized"))
             return@webSocket
         }*/
         this.call.attributes.put(CallAttributeKey, this.call)

         for(frame in   incoming){
             when(frame){

                 is Frame.Text -> {
                     val text = frame.readText()
                     //incoming.
                     //outgoing.send(Frame.Text("You said: $text"))
                 }
                 else -> {

                 }
             }
         }

         try {
             handler()
         } catch (e: Exception) {
             println("Error in WebSocket session: ${e.message}")
         } finally {
             println("apiWebSocket: Connection closed on $path")
         }
     }
}

class WebSocketServer (
    override val config: (Application.() -> Unit)? = null
) : RestServer(null) {
    companion object {
        val apiConfig : ApiConfig = getDefaultConfig()

        fun getDefaultConfig(): ApiConfig{
            return ApiConfig()
        }

        fun create(configure: (Application.() -> Unit)? = null): RestServer {
            return RestServer(configure)
        }

        fun start(host: String, port: Int, configure: (Application.() -> Unit)? = null) {
            create(configure).configureHost(host, port).start()
        }

        @OptIn(ExperimentalSerializationApi::class)
        private var  _jsonDefault :  Json = Json{

            serializersModule = SerializersModule {
                polymorphic(RequestData::class) {
                    subclass(DeleteRequestData::class, DeleteRequestData.serializer())
                }
            }
            classDiscriminator = "type"
            ignoreUnknownKeys = true
            decodeEnumsCaseInsensitive = true
        }
        var jsonDefault : Json   = _jsonDefault
            get() = _jsonDefault

        @OptIn(ExperimentalSerializationApi::class)
        fun setJsonDefault(builderAction  : (SerializersModuleBuilder.() -> Unit)? = null ) {

           val newInstance = Json {
                if (builderAction != null) {
                    serializersModule = SerializersModule(builderAction)
                }
                ignoreUnknownKeys = true
                decodeEnumsCaseInsensitive = true
            }
            this._jsonDefault = newInstance
        }

    }

    val activeUsers = ActiveUsers()

    init {
        super.onAuthenticated={
            if(it.success){
              val newUser =   ApiUser(it.id,"someuser").also { user->
                    user.setToken(it.token)
                }
                activeUsers.addUser(newUser)
            }
        }
    }

    fun configureWSHost(host: String, port: Int) {
        super.configureHost(host, port)
    }

    private fun configureContentNegotiation(application: Application): Application {
        application.apply {

        }
        return application
    }

    private fun configureWSRouting(route:  RoutingRoot){

    }

    fun configureWebSocketServer(application: Application): Application {

        application.apply {
            if (this.pluginOrNull(WebSockets) != null) {
                apiLogger.info("Custom socket installation present")
            }else{
               apiLogger.info("Installing default websocket")
               install(WebSockets){
                    pingPeriod =  Duration.parse("60s")
                    timeout = Duration.parse("15s")
                    maxFrameSize = Long.MAX_VALUE
                    masking = false
                   // contentConverter = KotlinxWebsocketSerializationConverter(Json)

                      extensions {
                          install(RouteContent)
                      }
                }
                apiLogger.info("Default websocket installed")
            }
            apiLogger.info("Websocket routing configured")

        }.let { return it }
    }

    override fun start(wait: Boolean){

        embeddedServer(Netty, port, host) {
            val restConfig = super.configureServer(this)
            configureWebSocketServer(restConfig)
            config?.invoke(this)
            apiLogger.info("Starting Rest API server on $host:$port")
        }.start(wait)

    }

}