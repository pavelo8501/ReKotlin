package po.api.rest_service

import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.ktor.server.routing.application
import io.ktor.util.AttributeKey
import io.ktor.util.toMap

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.json.Json

import po.api.rest_service.common.ApiLoginRequestDataContext
import po.api.rest_service.logger.LoggingService
import po.api.rest_service.models.ApiResponse
import po.api.rest_service.models.DefaultLoginRequest
import po.api.rest_service.models.DeleteRequestData
import po.api.rest_service.models.LoginRequestData
import po.api.rest_service.models.RequestData
import po.api.rest_service.models.SelectRequestData
import po.api.rest_service.models.UpdateRequestData
import po.api.rest_service.plugins.LoggingPlugin
import po.api.rest_service.plugins.PolymorphicJsonConverter
import po.api.rest_service.plugins.RateLimiter


val Application.apiLogger: LoggingService
    get() = attributes[RestServer.loggerKey]


class RestServer(
    private val configure: (Application.() -> Unit)? = null
) {
    companion object {

        val loggerKey = AttributeKey<LoggingService>("Logger")

        fun create(configure: (Application.() -> Unit)? = null): RestServer {
            return RestServer(configure)
        }

        fun start(host: String, port: Int, configure: (Application.() -> Unit)? = null) {
            create(configure).configureHost(host, port).start()
        }

        @OptIn(ExperimentalSerializationApi::class)
        fun jsonDefault(builderAction: SerializersModuleBuilder.() -> Unit): Json{
            val json = Json {
                serializersModule = SerializersModule(builderAction)
                classDiscriminator = "type"
                ignoreUnknownKeys = true
                decodeEnumsCaseInsensitive = true
            }
            return json
        }
    }

    private var _host: String = "0.0.0.0"
    val host: String
        get() = _host

    private var _port: Int = 8080
    val port: Int
        get() = _port

    val apiLogger: LoggingService = LoggingService()

    fun configureHost(host: String, port: Int): RestServer {
        this._host = host
        this._port = port
        return this
    }

    fun configure(application: Application) {
        application.apply {
            install(LoggingPlugin)
            apiLogger.info("Starting server initialization")

            configure?.invoke(this)


            install(RateLimiter) {
                requestsPerMinute = 60
            }


            apiLogger.info("Installing CORS")
            if (this.pluginOrNull(CORS) != null) {
                apiLogger.info("Custom CORS installed")
            } else {
                install(CORS) {
                    allowMethod(HttpMethod.Options)
                    allowMethod(HttpMethod.Get)
                    allowMethod(HttpMethod.Post)
                    allowHeader(HttpHeaders.ContentType)
                    allowHeader(HttpHeaders.Origin)
                    allowCredentials = true
                    anyHost()
                }
                apiLogger.info("Default CORS installed")
            }

            apiLogger.info("Installing ContentNegotiation")
            if (this.pluginOrNull(ContentNegotiation) != null) {
                apiLogger.info("Custom ContentNegotiation installed")
            } else {
                install(ContentNegotiation) {
                    //Register custom JSON converter, since the default one does not support polymorphic serialization
                    register(
                        ContentType.Application.Json,
                        PolymorphicJsonConverter(
                            jsonDefault() {
                                polymorphic(ApiLoginRequestDataContext::class) {
                                    subclass(DefaultLoginRequest::class, DefaultLoginRequest.serializer())
                                }
                                polymorphic(RequestData::class) {
                                    subclass(SelectRequestData::class, SelectRequestData.serializer())
                                    subclass(UpdateRequestData::class, UpdateRequestData.serializer())
                                    subclass(DeleteRequestData::class, DeleteRequestData.serializer())
                                    subclass(LoginRequestData::class, LoginRequestData.serializer())
                                }
                            }
                        )
                    )
                }
                apiLogger.info("Default ContentNegotiation installed")
            }

            apiLogger.info("Default rout initialization")


            routing {

                options("/api/status") {
                    call.response.header("Access-Control-Allow-Origin", "*")
                    call.respond(HttpStatusCode.OK)
                }

                get("/api/status") {
                    println("Accessing Application: ${application.hashCode()}")
                    try {
                        val logger = call.application.apiLogger
                        logger.info("Status endpoint called.")
                        call.respondText("OK")
                    } catch (e: Exception) {
                        println("Error accessing logger: ${e.message}")
                        call.respondText("Error accessing logger", status = HttpStatusCode.InternalServerError)
                    }
                }
                get("/api/status-json") {
                    try {
                        val logger = call.application.apiLogger
                        logger.info("Status Json endpoint called.")
                        val responseStatus : String = "OK"
                        call.respond(ApiResponse(responseStatus))
                    } catch (e: Exception) {
                        println("Error accessing logger: ${e.message}")
                        call.respondText("Error accessing logger", status = HttpStatusCode.InternalServerError)
                    }
                }

            }
            apiLogger.info("Default rout initialized")
            apiLogger.info("Server initialization complete")
        }
    }


    fun start(wait: Boolean = true) {
        embeddedServer(Netty, port, host) {
            configure(this)
            apiLogger.info("Starting Rest API server on $host:$port")
        }.start(wait)
    }
}