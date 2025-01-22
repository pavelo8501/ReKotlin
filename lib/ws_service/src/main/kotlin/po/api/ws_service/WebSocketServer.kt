package po.api.ws_service

import api.ws_service.service.security.ActiveUsers
import api.ws_service.service.security.ApiUser
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
import po.api.rest_service.*
import po.api.rest_service.server.ApiConfig
import kotlin.time.Duration

import po.api.rest_service.logger.LoggingService
import po.api.ws_service.plugins.ApiHeaderPlugin
import po.api.ws_service.plugins.WSApiContentConverter

import po.api.ws_service.service.extensions.TrafficController
import po.api.ws_service.service.plugins.Authenticator
import po.api.ws_service.service.plugins.PolymorphicJsonConverter
import po.api.ws_service.services.ConnectionService

//val webSocketMethodRegistryKey = AttributeKey<MutableList<WebSocketMethodRegistryItem>>("WebSocketMethod")

class WebSocketServer (
    private val config: (Application.() -> Unit)?
) : RestServer(null) {

    companion object {
        val apiConfig: ApiConfig = getDefaultConfig()

        fun getDefaultConfig(): ApiConfig {
            return ApiConfig()
        }

        fun create(configure: (Application.() -> Unit)? = null): RestServer {
            return RestServer(configure)
        }

        val connectionService =  ConnectionService
        lateinit var  apiLogger:  LoggingService

        @OptIn(ExperimentalSerializationApi::class)
        private var _jsonDefault: Json = Json {
            classDiscriminator = "type"
            ignoreUnknownKeys = true
            decodeEnumsCaseInsensitive = true
        }
        var jsonDefault: Json = _jsonDefault
            get() = _jsonDefault

        @OptIn(ExperimentalSerializationApi::class)
        fun setJsonDefault(builderAction: (SerializersModuleBuilder.() -> Unit)? = null) {

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

        super.onAuthenticated = {
            if (it.success) {
                val newUser = ApiUser(it.id, "someuser").also { user ->
                    user.setToken(it.token)
                }
                activeUsers.addUser(newUser)
            }
        }
    }

    fun configureWSHost(host: String, port: Int) {
        super.configureHost(host, port)
    }

    private fun configureDefaultHeaders(application: Application):Application{
       apiLogger.info("Configuring Api Headers")
       application.apply {
           if (this.pluginOrNull(ApiHeaderPlugin) != null) {
               println("Already installed")
           }else{
               install(ApiHeaderPlugin)
           }
       }
       return application
   }

    private fun configureCallLogging(application: Application):Application{
        application.apply {

        }
        return application
    }

    private fun configureContentNegotiation(
        application: Application,
        polymorphicConverter : PolymorphicJsonConverter): Application {
        application.apply {

          val contentConverter = WSApiContentConverter().create()


            if (this.pluginOrNull(contentConverter) != null) {
               println("Already installed")
            }else{
                install(contentConverter) {
                    register(polymorphicConverter)
                }
            }
        }
        return application
    }

    private fun configureSecurity(application: Application): Application {
        application.apply {
            install(Authenticator)
        }
        return application
    }

    fun configureWebSocketServer(application: Application): Application {

        application.apply {

           Companion.apiLogger = apiLogger

            configureSecurity(this)
            configureDefaultHeaders(this)
            configureCallLogging(this)

            if (this.pluginOrNull(WebSockets) != null) {
                apiLogger.info("Custom socket installation present")
            }else{
               apiLogger.info("Installing default websocket")

               install(WebSockets){
                    pingPeriod =  Duration.parse("60s")
                    timeout = Duration.parse("15s")
                    maxFrameSize = Long.MAX_VALUE
                    masking = false
                    contentConverter = PolymorphicJsonConverter(connectionService,null)
                      extensions {
                          install(TrafficController){

                          }
                      }
                }
                apiLogger.info("Default websocket installed")
            }

            apiLogger.info("Installing default ContentNegotiation")
          //  configureContentNegotiation(this,polymorphicConverter)
            apiLogger.info("Default ContentNegotiation installed")
            config?.invoke(this)
            apiLogger.info("Websocket routing configured")
        }
        return application
    }

    override fun start(host: String, port: Int,  wait: Boolean){
        super.start(host, port, wait)

//        embeddedServer(Netty, port, host) {
////            super.configureServer(this)
////            configureWebSocketServer(this)
////            apiLogger.info("Starting Rest API server on $host:$port")
//
//        }
    }

}