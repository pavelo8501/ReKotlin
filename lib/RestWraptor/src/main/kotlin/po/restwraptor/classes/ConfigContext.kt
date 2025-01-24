package po.restwraptor.classes

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.plugin
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
import po.restwraptor.exceptions.AuthErrorCodes
import po.restwraptor.interfaces.SecuredUserInterface
import po.restwraptor.models.configuration.ApiConfig
import po.restwraptor.models.request.ApiRequest
import po.restwraptor.models.request.LoginRequest
import po.restwraptor.models.response.Response
import po.restwraptor.models.security.AuthenticatedModel
import po.restwraptor.plugins.JWTPlugin
import po.restwraptor.plugins.RateLimiter
import po.restwraptor.security.JWTService

class ConfigContext(
    private val app: Application,
    config: ApiConfig? = null
): CanNotify{

    override val eventHandler = RootEventHandler("Server config")

    val apiConfig  = config?: ApiConfig()

    var onLoginRequest: ((LoginRequest) -> SecuredUserInterface?)? = null
    var onAuthenticated : ((AuthenticatedModel) -> Unit)? = null

    init {
        configCors()
        configContentNegotiation()
        configRateLimiter()
        configAuthentication()
        configDefaultRouting()
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
                                    jwtService.generateToken(user)?.let { token ->
                                        call.respond(Response(token))
                                        onAuthenticated?.invoke(AuthenticatedModel(token, true, 1))
                                    } ?: propagatedException("Token generation failed")
                                }
                                return@post
                            } else {
                                warn("Login failed for ${loginData.username} with password ${loginData.password}")
                            }
                        } ?: propagatedException("OnLoginRequest callback not set")
                    }
                }
            }
        }
    }

    private fun configAuthentication(){
        app.apply {
            if (this.pluginOrNull(Authentication) != null) {
                info("Authentication installation skipped. Custom Authentication already installed")
            } else {
                info("Installing JWT Plugin")
                install(JWTPlugin)
                install(Authentication) {
                    app.plugin(JWTPlugin).jwtService?.let { jwtService ->
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
                            if(apiConfig.enableDefaultSecurity) {
                                configureDefaultSecurityRoute(jwtService, this)
                            }
                        }
                    }?:propagatedException("JWT Plugin not installed. Unable to install Authentication")

                }
            }
        }
    }

    private fun configCors():Application{
        app.apply {
            if (this.pluginOrNull(CORS) != null) {
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
            if (this.pluginOrNull(RateLimiter) != null) {
                info("RateLimiter installation skipped. Custom RateLimiter already installed")
            }else{
                info("Installing RateLimiter")
                install(RateLimiter) {
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
                    call.respond(Response(responseStatus))
                }
            }
            info("Default rout initialized")
            return app
        }
    }

}