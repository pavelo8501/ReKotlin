package po.restwraptor.classes

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.contentType
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.options
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.eventhandler.interfaces.CanNotify
import po.lognotify.shared.enums.HandleType
import po.restwraptor.exceptions.ConfigurationErrorCodes
import po.restwraptor.exceptions.ConfigurationException
import po.restwraptor.interfaces.SecuredUserInterface
import po.restwraptor.models.configuration.ApiConfig
import po.restwraptor.models.request.ApiRequest
import po.restwraptor.models.request.LoginRequest
import po.restwraptor.models.response.ApiResponse
import po.restwraptor.models.security.AuthenticatedModel
import po.restwraptor.plugins.JWTPlugin
import po.restwraptor.plugins.RateLimiterPlugin
import po.restwraptor.security.JWTService

interface ConfigContextInterface{

    val apiConfig  :ApiConfig
    var onLoginRequest: ((LoginRequest) -> SecuredUserInterface?)?
    var onAuthenticated : ((AuthenticatedModel) -> Unit)?

    fun setupApi(configFn : ApiConfig.()-> Unit)
    fun setupApplication(block: Application.()->Unit)
    fun initialize(): Application

}


class ConfigContext(
    private var app: Application,
    config: ApiConfig? = null
): ConfigContextInterface,  CanNotify{

    override val eventHandler = RootEventHandler("Server config")
    override val apiConfig  = config?: ApiConfig()
    override var onLoginRequest: ((LoginRequest) -> SecuredUserInterface?)? = null
    override var onAuthenticated : ((AuthenticatedModel) -> Unit)? = null

    init {
        eventHandler.registerPropagateException<ConfigurationException>{
            ConfigurationException("Default Message", HandleType.PROPAGATE_TO_PARENT)
        }
    }

    private fun configureDefaultSecurityRoute(jwtService: JWTService, routing: Routing) {
        routing.apply {
            route("${apiConfig.baseApiRoute}/login") {
                post {
                    info("Request content type: ${call.request.contentType()}")
                    val loginRequest = call.receive<ApiRequest<LoginRequest>>()
                    loginRequest.data.let { loginData ->
                        onLoginRequest?.let {
                            val user = it.invoke(loginData)
                            if (user != null) {
                                if (apiConfig.useWellKnownHost) {
                                    TODO("Use well known hosts logic not implemented")
                                } else {
                                    jwtService.generateToken(user).let { token ->
                                        call.respond(ApiResponse(token))
                                        onAuthenticated?.invoke(AuthenticatedModel(token, true, 1))
                                    } ?: propagatedException<ConfigurationException>("Token generation failed"){
                                        errorCode = ConfigurationErrorCodes.PLUGIN_SETUP_FAILURE
                                    }
                                }
                                return@post
                            } else {
                                warn("Login failed for ${loginData.username} with password ${loginData.password}")
                            }
                        } ?: propagatedException<ConfigurationException>("OnLoginRequest callback not set"){
                            errorCode = ConfigurationErrorCodes.UNABLE_TO_CALLBACK
                        }
                    }
                }
            }
        }
    }

    private fun configJwt(): JWTService?{
        app.apply {
            install(JWTPlugin) {
                privateKeyString = apiConfig.privateKeyString
                publicKeyString  = apiConfig.publicKeyString
            }.let {plugin->
                plugin.jwtService.let{service->
                    return service
                }
            }
        }
        return null
    }

    private fun configAuthentication(){
        app.apply {
            if (this.pluginOrNull(Authentication) != null) {
                info("Authentication installation skipped. Custom Authentication already installed")
            } else {
                info("Installing JWT Plugin")
                configJwt()?.let { jwtService->
                    install(Authentication) {
                        jwt("auth-jwt") {
                            realm = jwtService.realm
                            verifier(jwtService.getVerifier())
                            validate { credential ->
                                if (credential.payload.audience.contains(jwtService.audience)) {
                                    JWTPrincipal(credential.payload)
                                } else null
                            }
                        }
                        app.routing {
                            if (apiConfig.enableDefaultSecurity) {
                                configureDefaultSecurityRoute(jwtService, this)
                            }
                        }
                    }
                }?:propagatedException<ConfigurationException>(
                    "JWT Plugin not installed. Unable to install Authentication"){
                    errorCode = ConfigurationErrorCodes.REQUESTING_UNDEFINED_PLUGIN
                }
            }
        }
    }

    private fun configCors():Application{
        app.apply {
            if (pluginOrNull(CORS) != null) {
                info("CORS installation skipped. Custom CORS already installed")
            } else {
                info("Installing CORS Plugin")
                install(CORS) {
                    allowMethod(HttpMethod.Options)
                    allowMethod(HttpMethod.Get)
                    allowMethod(HttpMethod.Post)
                    allowHeader(HttpHeaders.ContentType)
                    allowHeader(HttpHeaders.Origin)
                    allowCredentials = true
                    anyHost()
                }
                info("Default CORS installed")
            }
        }
        return app
    }

    private fun configContentNegotiation():Application{
        app.apply {
            if (this.pluginOrNull(ContentNegotiation) != null) {
                info("ContentNegotiation installation skipped. Custom ContentNegotiation already installed")
            } else {
                info("Installing Default ContentNegotiation")
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                        encodeDefaults = true
                    })
                }
                info("Default ContentNegotiation installed")
            }
        }
        return app
    }

    private fun configRateLimiter():Application{
        app.apply {
            if (this.pluginOrNull(RateLimiterPlugin) != null) {
                info("RateLimiter installation skipped. Custom RateLimiter already installed")
            }else{
                info("Installing RateLimiter")
                install(RateLimiterPlugin) {
                    requestsPerMinute = 60
                    suspendInSeconds = 60
                }
                info("RateLimiter installed")
            }
        }
        return app
    }

    private fun configDefaultRouting(): Application {
        info("Default rout initialization")
        app.apply {
            routing {
                options("/api/status") {
                    call.response.header("Access-Control-Allow-Origin", "*")
                    call.respond(HttpStatusCode.OK)
                }
                get("/api/status") {
                    info("Accessing Application: ${application.hashCode()}")
                    call.respondText("OK")
                }
                get("/api/status-json") {
                    info("Status Json endpoint called.")
                    val responseStatus: String = "OK"
                    call.respond(ApiResponse(responseStatus))
                }
            }
            info("Default rout initialized")
            return app
        }
    }

    override fun setupApi(configFn : ApiConfig.()-> Unit){
        apiConfig.configFn()
    }
    override fun setupApplication(block: Application.()->Unit){
        app.block()
    }

    override fun initialize(): Application{
        configCors()
        configContentNegotiation()
        configRateLimiter()
        configAuthentication()
        configDefaultRouting()
        return app
    }
}