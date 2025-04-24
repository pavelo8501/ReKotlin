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
import po.lognotify.extensions.lastTaskHandler
import po.lognotify.extensions.subTask
import po.restwraptor.RestWrapTor
import po.restwraptor.models.configuration.AuthConfig
import po.restwraptor.models.configuration.WraptorConfig
import po.restwraptor.plugins.CoreAuthApplicationPlugin
import po.restwraptor.plugins.RateLimiterPlugin
import po.restwraptor.routes.configureSystemRoutes

interface ConfigContextInterface{
    suspend fun setupAuthentication(
        cryptoKeys: CryptoRsaKeys,
        userLookupFn: suspend ((login: String)-> AuthenticationPrincipal?),
        configFn  : suspend AuthConfigContext.()-> Unit)
    suspend fun setup(configFn : suspend WraptorConfig.()-> Unit)
    suspend fun setup(configuration : WraptorConfig, configFn : suspend WraptorConfig.()-> Unit)
    fun setupApplication(block: Application.()->Unit)
    suspend fun initialize():Application
}

class ConfigContext(
    internal val wraptor : RestWrapTor,
    internal val wrapConfig : WraptorConfig,
    internal val authConfig : AuthConfig,

): ConfigContextInterface, TasksManaged{

    val personalName = "ConfigContext"

    internal val apiConfig  =  wrapConfig.apiConfig
    private val application : Application  by lazy { wraptor.application }
    private val authContext  : AuthConfigContext by lazy { AuthConfigContext(application, authConfig) }

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

    internal fun getThisTaskHandler(): TaskHandler<*>{
        return lastTaskHandler()
    }

    /**
     * Configures parameters that must be applied before custom configuration and user defined one.
     */
    private fun configAppParams(app: Application){
        app.rootPath = apiConfig.baseApiRoute
    }

    override suspend fun setupAuthentication(
        cryptoKeys: CryptoRsaKeys,
        userLookupFn: suspend ((login: String)-> AuthenticationPrincipal?),
        configFn  : suspend AuthConfigContext.()-> Unit){
        authContext.setupAuthentication(cryptoKeys, userLookupFn)
        authContext.configFn()
    }
    override suspend fun setup(configuration : WraptorConfig, configFn : suspend WraptorConfig.()-> Unit){
        wrapConfig.configFn()
    }

    override suspend fun setup(configFn : suspend WraptorConfig.()-> Unit){
        wrapConfig.configFn()
    }

    override fun setupApplication(block: Application.()->Unit) {
        configAppParams(application)
        application.block()
    }

    private suspend fun installCoreAuth(app: Application){
        subTask("InstallCoreAuth"){handler ->
            app.apply {
                install(CoreAuthApplicationPlugin) {
                    headerName = HttpHeaders.Authorization
                    pluginKey = authConfig.jwtServiceName
                }
                handler.info("CoreAuthPlugin Plugin installed")
            }
        }
    }

    override suspend fun initialize(): Application{

        installCoreAuth(application)

        subTask("Initialization", personalName){
            if(apiConfig.cors){
                configCors(application)
            }
            if(apiConfig.contentNegotiation){
                configContentNegotiation(application)
            }
            if(apiConfig.rateLimiting){
                configRateLimiter(application)
            }

            if (apiConfig.systemRouts) {
                application.routing {
                    configureSystemRoutes(apiConfig.baseApiRoute, this@ConfigContext)
                }
            }
        }
        return application
    }
}