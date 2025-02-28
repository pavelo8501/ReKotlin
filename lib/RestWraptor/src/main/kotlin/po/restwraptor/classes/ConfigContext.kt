package po.restwraptor.classes

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
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
import po.restwraptor.RestWrapTor
import po.restwraptor.builders.restWrapTor
import po.restwraptor.exceptions.ConfigurationErrorCodes
import po.restwraptor.exceptions.ConfigurationException
import po.restwraptor.interfaces.SecuredUserInterface
import po.restwraptor.models.configuration.ApiConfig
import po.restwraptor.models.configuration.AuthenticationConfig
import po.restwraptor.models.configuration.WraptorConfig
import po.restwraptor.models.request.ApiRequest
import po.restwraptor.models.request.LoginRequest
import po.restwraptor.models.response.ApiResponse
import po.restwraptor.models.security.AuthenticatedModel
import po.restwraptor.plugins.JWTPlugin
import po.restwraptor.plugins.RateLimiterPlugin
import po.restwraptor.security.JWTService

interface ConfigContextInterface{
    fun setupAuthentication(configFn : AuthenticationContext.()-> Unit)
    fun configSettings(configFn : WraptorConfig.()-> Unit)
    fun setupApplication(block: Application.()->Unit)
    fun initialize()
}

class ConfigContext(
    internal val wraptor : RestWrapTor,
    private val wrapConfig : WraptorConfig,
): ConfigContextInterface,  CanNotify{

    override val eventHandler = RootEventHandler("Server config")
    internal val apiConfig  =  wrapConfig.apiConfig
    private val authContext  : AuthenticationContext by lazy { AuthenticationContext( wraptor.eventHandler, this) }
    internal val app : Application  by lazy { wraptor.application }

    internal val jsonFormatter : Json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    init {

        eventHandler.registerPropagateException<ConfigurationException>{
            ConfigurationException("Default Message", HandleType.PROPAGATE_TO_PARENT)
        }
    }

    private fun configCors():Application{
        app.apply {
            if (pluginOrNull(CORS) != null) {
                info("CORS installation skipped. Custom CORS already installed")
            } else {
                info("Installing CORS Plugin")
                install(CORS) {
                    allowNonSimpleContentTypes
                    allowMethod(HttpMethod.Options)
                    allowMethod(HttpMethod.Get)
                    allowMethod(HttpMethod.Post)
                    allowMethod(HttpMethod.Put)
                    allowMethod(HttpMethod.Patch)
                    allowHeader(HttpHeaders.Authorization)
                    allowHeader(HttpHeaders.ContentType)
                    allowHeader(HttpHeaders.Origin)
                    allowCredentials = true
                    anyHost()
                }
                println("Default CORS installed")
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
                    json(jsonFormatter)
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

    private fun configSystemRoutes(): Application {
        info("Default rout initialization")
        app.apply {
            routing {
                options("${apiConfig.baseApiRoute}/status") {
                    call.response.header("Access-Control-Allow-Origin", "*")
                    call.respond(HttpStatusCode.OK)
                }
                get("${apiConfig.baseApiRoute}/status") {
                    info("Accessing Application: ${application.hashCode()}")
                    call.respondText("OK")
                }
                get("${apiConfig.baseApiRoute}/status-json") {
                    info("Status Json endpoint called.")
                    val responseStatus: String = "OK"
                    call.respond(ApiResponse(responseStatus))
                }
            }
            info("Default rout initialized")
            return app
        }
    }

    override fun setupAuthentication(configFn : AuthenticationContext.()-> Unit){
        authContext.configFn()
    }
    override fun configSettings(configFn : WraptorConfig.()-> Unit){
        wrapConfig.configFn()
    }
    override fun setupApplication(block: Application.()->Unit){
        app.block()
    }

    override fun initialize(){
        if(apiConfig.cors){
            configCors()
        }
        if(apiConfig.contentNegotiation){
            configContentNegotiation()
        }
        if(apiConfig.rateLimiting){
            configRateLimiter()
        }
        if(apiConfig.systemRouts){
            configSystemRoutes()
        }
    }
}