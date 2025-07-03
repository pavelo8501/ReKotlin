package po.restwraptor.scope

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.models.CryptoRsaKeys
import po.lognotify.TasksManaged
import po.lognotify.classes.task.TaskHandler
import po.lognotify.extensions.subTask
import po.restwraptor.RestWrapTor
import po.restwraptor.models.configuration.ApiConfig
import po.restwraptor.models.configuration.AuthConfig
import po.restwraptor.models.configuration.WraptorConfig
import po.restwraptor.plugins.CoreAuthApplicationPlugin
import po.restwraptor.plugins.RateLimiterPlugin
import po.restwraptor.routes.configureSystemRoutes

interface ConfigContextInterface{
    suspend fun setupAuthentication(
        cryptoKeys: CryptoRsaKeys,
        userLookupFn: suspend ((login: String)-> AuthenticationPrincipal?),
        configFn  : (suspend AuthConfigContext.()-> Unit)? = null)
}

class ConfigContext(
    internal val wraptor : RestWrapTor,
    private val wrapConfig : WraptorConfig,

    ): ConfigContextInterface, TasksManaged{

    override val contextName: String = "ConfigContext"

    var apiConfig : ApiConfig
        get()  {return  wrapConfig.apiConfig}
        set(value){
            wrapConfig.apiConfig = value
        }

    var authConfig : AuthConfig
        get()  {return  wrapConfig.authConfig}
        set(value){
            wrapConfig.authConfig = value
        }

    private val application : Application  by lazy { wraptor.application }
    private val authContext  : AuthConfigContext  by lazy { AuthConfigContext(application, wrapConfig) }

    internal val jsonFormatter : Json = Json {
        isLenient = true
        encodeDefaults = true
    }

    private suspend fun configCors(app: Application):Application{
        subTask("ConfigCors") {handler->
            app.apply {
                if (pluginOrNull(CORS) != null) {
                    handler.info("CORS installation skipped. Custom CORS already installed")
                } else {
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
                    handler.info("CORS installed")
                }
            }
        }

        return app
    }
    private suspend fun configContentNegotiation(app: Application):Application{
        subTask("ConfigContentNegotiation") { handler ->
            app.apply {
                if (this.pluginOrNull(ContentNegotiation) != null) {
                    handler.info("ContentNegotiation installation skipped. Custom ContentNegotiation already installed")
                } else {
                    handler.info("Installing Default ContentNegotiation")
                    install(ContentNegotiation) {
                        json(jsonFormatter)
                    }
                    handler.info("Default ContentNegotiation installed")
                }
            }
        }
        return app
    }
    private suspend fun configRateLimiter(app: Application):Application{
        subTask("ConfigRateLimiter") { handler ->
            app.apply {
                if (this.pluginOrNull(RateLimiterPlugin) != null) {
                    handler.info("RateLimiter installation skipped. Custom RateLimiter already installed")
                } else {
                    install(RateLimiterPlugin) {
                        requestsPerMinute = 60
                        suspendInSeconds = 60
                    }
                    handler.info("RateLimiter installed")
                }
            }
        }
        return app
    }



   // private var userLookupFn: (suspend(login: String)-> AuthenticationPrincipal?)? = null
    private var authConfigFn:  (suspend AuthConfigContext.()-> Unit)? = null

    fun applyApiConfig(config: ApiConfig){
        wrapConfig.apiConfig = config
    }
    fun applyAuthConfig(config: AuthConfig){
        wrapConfig.authConfig = config
    }

    var applicationConfigFn : (Application.()-> Unit)? = null
    fun setupApplication(appConfigFn : Application.()-> Unit){
        applicationConfigFn  = appConfigFn
    }
    override suspend fun setupAuthentication(
        cryptoKeys: CryptoRsaKeys,
        userLookupFn: suspend ((login: String)-> AuthenticationPrincipal?),
        configFn  : (suspend AuthConfigContext.()-> Unit)?){
        authContext.setupAuthentication(cryptoKeys,userLookupFn)
        authConfigFn = configFn
    }

    private suspend fun installCoreAuth(app: Application){
        subTask("InstallCoreAuth"){handler ->
            app.apply {
                install(CoreAuthApplicationPlugin) {
                    headerName = HttpHeaders.Authorization
                    pluginKey =  wrapConfig.authConfig.jwtServiceName
                }
                handler.info("CoreAuthPlugin Plugin installed")
            }
        }
    }

    internal suspend fun initialize(builderFn: (suspend  ConfigContext.()-> Unit)?): Application{
        builderFn?.invoke(this)
        installCoreAuth(application)
        applicationConfigFn?.invoke(application)
        subTask("Initialization"){
            if(apiConfig.cors){ configCors(application) }
            if(apiConfig.contentNegotiation){ configContentNegotiation(application) }
            if(apiConfig.rateLimiting){ configRateLimiter(application) }
            if (apiConfig.systemRouts) {
                application.routing {
                    configureSystemRoutes(apiConfig.baseApiRoute, this@ConfigContext)
                }
            }
        }
        authConfigFn?.invoke(authContext)
        return application
    }
}