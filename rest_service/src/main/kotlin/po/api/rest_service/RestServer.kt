package po.api.rest_service

import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.ktor.util.AttributeKey
import jdk.tools.jlink.resources.plugins

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.json.Json

import po.api.rest_service.common.ApiLoginRequestDataContext
import po.api.rest_service.common.SecureUserContext
import po.api.rest_service.exceptions.AuthErrorCodes
import po.api.rest_service.exceptions.AuthException
import po.api.rest_service.exceptions.ConfigurationErrorCodes
import po.api.rest_service.exceptions.ConfigurationException
import po.api.rest_service.exceptions.DataErrorCodes
import po.api.rest_service.exceptions.DataException
import po.api.rest_service.logger.LoggingService
import po.api.rest_service.models.ApiRequest
import po.api.rest_service.models.ApiResponse
import po.api.rest_service.models.DefaultLoginRequest
import po.api.rest_service.models.DeleteRequestData
import po.api.rest_service.models.LoginRequestData
import po.api.rest_service.models.RequestData
import po.api.rest_service.models.SelectRequestData
import po.api.rest_service.models.UpdateRequestData
import po.api.rest_service.plugins.Jwt
import po.api.rest_service.plugins.LoggingPlugin
import po.api.rest_service.plugins.PolymorphicJsonConverter
import po.api.rest_service.plugins.RateLimiter
import po.api.rest_service.security.JWTService
import po.api.rest_service.server.ApiConfig


val Application.apiLogger: LoggingService
    get() = attributes[RestServer.loggerKey]

val Application.jwtService: JWTService
    get() = plugin(Jwt).jwtService?: throw ConfigurationException(ConfigurationErrorCodes.REQUESTING_UNDEFINED_PLUGIN, "JWT plugin not found or JwtService not initialized")


class RestServer(
    private val configure: (Application.() -> Unit)? = null
) {
    companion object {

        val loggerKey = AttributeKey<LoggingService>("Logger")

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

    var onLoginRequest: ((LoginRequestData) -> SecureUserContext?)? = null

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


            if (this.pluginOrNull(Jwt) != null) {
                apiLogger.info("Custom JWT installed")
            } else {
                if(apiConfig.enableDefaultJwt) {
                    apiLogger.info("Installing default JWT")
                    install(Jwt)
                    apiLogger.info("Default JWT installed")
                }else {
                    apiLogger.info("Skip JWT installation")
                }
            }

            if (apiConfig.enableRateLimiting) {
                apiLogger.info("Installing RateLimiter")
                install(RateLimiter) {
                    requestsPerMinute = 60
                    suspendInSeconds = 60
                }
                apiLogger.info("RateLimiter installed")
            }else{
                apiLogger.info("Skip Rate Limiter installation")
            }

            apiLogger.info("Installing CORS")
            if (this.pluginOrNull(CORS) != null) {
                apiLogger.info("Custom CORS installed")
            } else {
                if(apiConfig.enableDefaultCors) {
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
                }else{
                    apiLogger.info("Skip CORS installation")
                }
            }

            apiLogger.info("Installing ContentNegotiation")
            if (this.pluginOrNull(ContentNegotiation) != null) {
                apiLogger.info("Custom ContentNegotiation installed")
            } else {
                if (apiConfig.enableDefaultContentNegotiation) {
                    install(ContentNegotiation) {
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
                } else {
                    apiLogger.info("Skip ContentNegotiation installation")
                }
            }

            apiLogger.info("Default rout initialization")


            routing {

                if(apiConfig.enableDefaultSecurity){
                    route("${apiConfig.baseApiRoute}/login") {
                        post {
                            try {
                              //  apiLogger.info("Request content type: ${call.request.contentType()}")
                                val loginRequest = call.receive<ApiRequest<RequestData>>()
                                if (loginRequest.data !is LoginRequestData) {
                                    throw DataException(DataErrorCodes.REQUEST_DATA_MISMATCH, "Not a login request")
                                }
                                (loginRequest.data).let { loginData ->
                                    if(onLoginRequest == null){
                                        throw ConfigurationException(ConfigurationErrorCodes.UNABLE_TO_CALLBACK, "onLoginRequest callback not set")
                                    }
                                    val user = onLoginRequest!!.invoke(loginData)
                                    if(user == null){
                                        apiLogger.action("Login failed for ${loginData.value.username} with password ${loginData.value.password}")
                                        call.response.status(HttpStatusCode.Unauthorized)
                                        call.respond(ApiResponse(null).setErrorMessage(AuthErrorCodes.INVALID_CREDENTIALS.code,"Authentication failed"))
                                        return@post
                                    }
                                    if(apiConfig.useWellKnownHost){

                                    }else{
                                        jwtService.generateToken(user).let { token ->
                                            if (token == null) {
                                                apiLogger.error("Token generation failed",AuthException(AuthErrorCodes.TOKEN_GENERATION_FAILED, "Token generation failed"))
                                            }
                                            //Finally got to success point
                                            call.respond(ApiResponse(token))
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                apiLogger.error(e.message?: "Internal server error")
                                call.response.status(HttpStatusCode.InternalServerError)
                                call.respond(ApiResponse(null).setErrorMessage(500,"Internal server error"))
                            }
                        }
                    }
                }

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