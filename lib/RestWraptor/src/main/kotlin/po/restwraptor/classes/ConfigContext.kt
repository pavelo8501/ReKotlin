package po.restwraptor.classes

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.options
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import po.restwraptor.RestWrapTor
import po.restwraptor.classes.convenience.respondNotFound
import po.restwraptor.enums.EnvironmentType
import po.restwraptor.models.configuration.WraptorConfig
import po.restwraptor.models.response.ApiResponse
import po.restwraptor.plugins.RateLimiterPlugin
import po.restwraptor.plugins.ReplyInterceptorPlugin

interface ConfigContextInterface{
    fun setupAuthentication(configFn : AutConfigContext.()-> Unit)
    fun setup(configFn : WraptorConfig.()-> Unit)
    fun setupApplication(block: Application.()->Unit)
    fun initialize()
}

class ConfigContext(
    internal val wraptor : RestWrapTor,
    private val wrapConfig : WraptorConfig,
): ConfigContextInterface{

    internal val apiConfig  =  wrapConfig.apiConfig
    private val authContext  : AutConfigContext by lazy { AutConfigContext(this) }
    internal val app : Application  by lazy { wraptor.application }

    internal val jsonFormatter : Json = Json {
        isLenient = true
        encodeDefaults = true
    }

    private fun configCustomPlugins(app : Application){
        app.apply {
            install(ReplyInterceptorPlugin){
            }
        }
    }

    private fun configCors():Application{
        app.apply {
            if (pluginOrNull(CORS) != null) {
               // info("CORS installation skipped. Custom CORS already installed")
            } else {
               // info("Installing CORS Plugin")
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
               // info("Default CORS installed")
            }
        }
        return app
    }

    private fun configContentNegotiation():Application{
        app.apply {
            if (this.pluginOrNull(ContentNegotiation) != null) {
               // info("ContentNegotiation installation skipped. Custom ContentNegotiation already installed")
            } else {
               // info("Installing Default ContentNegotiation")
               install(ContentNegotiation) {
                    json(jsonFormatter)
                }
            //    install(FlexibleContentNegotiationPlugin)

              // info("Default ContentNegotiation installed")
            }
        }
        return app
    }

    private fun configRateLimiter():Application{
        app.apply {
            if (this.pluginOrNull(RateLimiterPlugin) != null) {
               // info("RateLimiter installation skipped. Custom RateLimiter already installed")
            }else{
              //  info("Installing RateLimiter")
                install(RateLimiterPlugin) {
                    requestsPerMinute = 60
                    suspendInSeconds = 60
                }
             //   info("RateLimiter installed")
            }
        }
        return app
    }

    private fun configSystemRoutes(): Application {
      //  info("Default rout initialization")
        app.apply {
            routing {
                options("/status") {
                    call.response.header("Access-Control-Allow-Origin", "*")
                    call.respondText("OK")
                }
                get("/status") {
                  //  info("Accessing Application: ${application.hashCode()}")
                    call.respond(wraptor.status().toString())
                }
                get("/status-json") {
                  //  info("Status Json endpoint called.")
                    val responseStatus: String = "OK"
                    call.respond(ApiResponse(responseStatus))
                }

                route("{...}") {
                    handle {
                       if(wrapConfig.enviromnent != EnvironmentType.PROD)  {
                           call.respondNotFound(
                               wraptor.getRoutes().map { ("Path: ${it.path}  Method: ${it.selector} IsSecured: ${it.isSecured} ")}
                           )
                       }else{
                           call.respondNotFound(listOf("Oops! This path does not exist."))
                       }
                    }
                }
            }
           // info("Default rout initialized")
            return app
        }
    }

    /**
     * Configures parameters that must be applied before custom configuration and user defined one.
     */
    private fun configAppParams(app: Application){
        app.rootPath = apiConfig.baseApiRoute
    }

    override fun setupAuthentication(configFn : AutConfigContext.()-> Unit){
        authContext.configFn()
    }
    override fun setup(configFn : WraptorConfig.()-> Unit){
        wrapConfig.configFn()
    }
    override fun setupApplication(block: Application.()->Unit){
        configAppParams(app)
        app.block()
    }

    override fun initialize(){
        configCustomPlugins(app)
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