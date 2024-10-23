package po.api.ws_service

import api.ws_service.service.security.ActiveUsers
import api.ws_service.service.security.ApiUser
import po.api.ws_service.service.extensions.TrafficController
import po.api.ws_service.service.models.DeleteRequestData
import po.api.ws_service.service.models.RequestData
import po.api.ws_service.service.routing.ApiWebSocketClass
import po.api.ws_service.service.routing.ApiWebSocketMethodClass
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.json.Json

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import kotlinx.serialization.modules.polymorphic
import po.api.rest_service.*
import po.api.rest_service.server.ApiConfig
import kotlin.time.Duration

import io.ktor.server.application.ApplicationCall
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.AttributeKey



val CallAttributeKey = AttributeKey<ApplicationCall>("call")
class WebSocketMethodRegistryItem(var  method: String, body: suspend WebSocketMethodDataContext.() -> Unit)

class WebSocketMethodDataContext(){
    val receiveApiRequest : (suspend (Unit) -> Unit)? = null

    suspend fun sendApiRequest(){

    }
}

val webSocketMethodRegistryKey = AttributeKey<MutableList<WebSocketMethodRegistryItem>>("WebSocketMethod")


class WebSocketServer (
    private val config: (Application.() -> Unit)?
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
              val newUser = ApiUser(it.id, "someuser").also { user->
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

    private fun configureWSRouting(application:  Application){
        ApiWebSocketMethodClass.registerListener(ApiWebSocketClass)
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
                          install(TrafficController){

                          }
                      }
                }
                apiLogger.info("Default websocket installed")
            }


            configureWSRouting(this)
            config?.invoke(this)

            apiLogger.info("Websocket routing configured")
        }
        return application
    }

    override fun start(wait: Boolean){
        embeddedServer(Netty, port, host) {
            super.configureServer(this)
            configureWebSocketServer(this)
            apiLogger.info("Starting Rest API server on $host:$port")
        }.start(wait)
    }

}