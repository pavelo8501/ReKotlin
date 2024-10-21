package po.api.ws_service

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.json.Json

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.serializersModuleOf
import po.api.rest_service.*
import po.api.rest_service.models.CreateRequestData
import po.api.rest_service.models.DeleteRequestData
import po.api.rest_service.models.RequestData
import po.api.rest_service.models.SelectRequestData
import po.api.rest_service.models.UpdateRequestData
import po.api.rest_service.server.ApiConfig
import po.api.ws_service.models.WSApiRequest
import po.api.ws_service.models.WSApiResponse
import po.api.ws_service.plugins.sendCloseReason
import kotlin.time.Duration


class WebSocketServer (
    private val configure: (Application.() -> Unit)? = null
) : RestServer(configure)  {
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
                    subclass(CreateRequestData::class, CreateRequestData.serializer())
                    subclass(SelectRequestData::class, SelectRequestData.serializer())
                    subclass(UpdateRequestData::class, UpdateRequestData.serializer())
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

    fun configureWSHost(host: String, port: Int) {
        super.configureHost(host, port)
    }

    private fun configureWSRouting(application: Application):Application{

        application.apply {
            routing {
                webSocket("/ws/status") {
                    try {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val receivedText = frame.readText()
                                WebSocketServer
                                var request =  jsonDefault.decodeFromString<WSApiRequest<RequestData>>(receivedText)

                                val requestText = "Vsje chetko ty prislal ${(request.data as SelectRequestData).value} ${request.module} and ${request.action}"
                                val response : WSApiResponse<String> = WSApiResponse<String>(requestText)
                                val responseText = jsonDefault.encodeToString<WSApiResponse<String>>(response)
                                send(Frame.Text(responseText))

                            }
                        }
                    } catch (e: Exception) {
                        sendCloseReason("Error occurred: ${e.message}")
                    }
                }
            }
        }
        return application
    }

    override fun configure(application: Application): Application {
        super.configure(application).apply {


            configure?.invoke(this)


            if (this.pluginOrNull(WebSockets) != null) {
                apiLogger.info("Custom socket installation present")
            }else{
                apiLogger.info("Installing default websocket")
                install(WebSockets){
                    pingPeriod =  Duration.parse("60s")
                    timeout = Duration.parse("15s")
                    maxFrameSize = Long.MAX_VALUE
                    masking = false
                }
                apiLogger.info("Default websocket installed")
            }


            configureWSRouting(this).apply {
                apiLogger.info("Websocket routing configured")
            }



        }.let { return it }
    }

    fun wsStart(wait: Boolean = true) {
        super.start(wait)
    }

}