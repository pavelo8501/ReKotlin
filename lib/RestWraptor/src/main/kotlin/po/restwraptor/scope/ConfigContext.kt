package po.restwraptor.scope

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.models.CryptoRsaKeys
import po.lognotify.TasksManaged
import po.lognotify.launchers.runAction
import po.misc.containers.BackingContainer
import po.misc.containers.backingContainerOf
import po.misc.context.CTXIdentity
import po.misc.context.asSubIdentity
import po.misc.functions.registries.builders.notifierRegistryOf
import po.restwraptor.RestWrapTor
import po.restwraptor.interfaces.WraptorResponse
import po.restwraptor.models.configuration.ApiConfig
import po.restwraptor.models.configuration.AuthConfig
import po.restwraptor.models.configuration.WraptorConfig
import po.restwraptor.models.response.DefaultResponse
import po.restwraptor.plugins.CallInterceptorPlugin
import po.restwraptor.plugins.CoreAuthApplicationPlugin
import po.restwraptor.plugins.RateLimiterPlugin
import po.restwraptor.routes.ManagedRoute
import po.restwraptor.routes.ManagedRouting
import po.restwraptor.routes.configureSystemRoutes


class ConfigContext(
    internal val wraptor : RestWrapTor,
    internal val application: Application
): TasksManaged {

    val wrapConfig: WraptorConfig = WraptorConfig()
    val coreContext: CoreContext = CoreContext(application, wraptor)

    override val identity: CTXIdentity<ConfigContext> = asSubIdentity(wraptor)
    internal val responseProvider: BackingContainer<() -> WraptorResponse<*>> = backingContainerOf()

    var apiConfig: ApiConfig
        get() {
            return wrapConfig.apiConfig
        }
        set(value) {
            wrapConfig.apiConfig = value
        }

    var authConfig: AuthConfig
        get() {
            return wrapConfig.authConfig
        }
        set(value) {
            wrapConfig.authConfig = value
        }

    private val authContext: AuthConfigContext = AuthConfigContext(application, wrapConfig)

    internal val managedRoutesRegistry = notifierRegistryOf<ManagedRouting>()

    internal val jsonFormatter: Json = Json {
        isLenient = true
        encodeDefaults = true
    }

    private var authConfigFn: (AuthConfigContext.() -> Unit)? = null

    val managedRouting: ManagedRouting = ManagedRouting()

    init {
        responseProvider.provideValue({ DefaultResponse("") })
    }

    private fun configCors(app: Application) = runAction("ConfigCors") {
        app.apply {
            if (pluginOrNull(CORS) != null) {
                notify("CORS installation skipped. Custom CORS already installed")
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
                notify("CORS installed")
            }
        }
    }

    private fun configContentNegotiation(app: Application) = runAction("ConfigContentNegotiation") {
        app.apply {
            if (this.pluginOrNull(ContentNegotiation) != null) {
                notify("ContentNegotiation installation skipped. Custom ContentNegotiation already installed")
            } else {
                notify("Installing Default ContentNegotiation")
                install(ContentNegotiation) {
                    json(jsonFormatter)
                }
                notify("Default ContentNegotiation installed")
            }
        }
    }

    private fun configRateLimiter(app: Application) = runAction("ConfigRateLimiter") {
        app.apply {
            if (this.pluginOrNull(RateLimiterPlugin) != null) {
                notify("RateLimiter installation skipped. Custom RateLimiter already installed")
            } else {
                install(RateLimiterPlugin) {
                    requestsPerMinute = 60
                    suspendInSeconds = 60
                }
                notify("RateLimiter installed")
            }
        }
    }

    private fun configCallInterceptor(app: Application) = runAction("ConfigCallInterceptor") {
        app.apply {
            install(CallInterceptorPlugin) {
                notify("CallInterceptor installed")
            }
        }
    }

    private fun installCoreAuth(app: Application) = runAction("InstallCoreAuth") {

        app.apply {
            install(CoreAuthApplicationPlugin) {
                headerName = HttpHeaders.Authorization
                pluginKey = wrapConfig.authConfig.jwtServiceName
            }
            notify("CoreAuthPlugin Plugin installed")
        }
    }

    private fun initializeManagedRoutes(app: Application){
        app.apply {
            routing {
                managedRoutesRegistry.trigger(managedRouting)
                managedRouting.provideRouteKtorRouting(this)
            }
        }
    }

    internal fun initialize(): Application = runAction("Initialization") {

        initializeManagedRoutes(application)

        configCallInterceptor(application)
        installCoreAuth(application)

        if (apiConfig.cors) {
            configCors(application)
        }
        if (apiConfig.contentNegotiation) {
            configContentNegotiation(application)
        }
        if (apiConfig.rateLimiting) {
            configRateLimiter(application)
        }
        if (apiConfig.systemRouts) {
            application.routing {
                configureSystemRoutes(this@ConfigContext, responseProvider.getValue(this))
            }
        }
        authConfigFn?.invoke(authContext)
        application
    }

    fun setupRoutes(block: ManagedRouting.()-> Unit){
        managedRoutesRegistry.subscribe(this,  block)
    }

    fun registerResponseProvider(provider: () -> WraptorResponse<*>) {
        responseProvider.provideValue(provider)
    }

    fun setupAuthentication(
        cryptoKeys: CryptoRsaKeys,
        userLookupFn: (suspend (login: String) -> AuthenticationPrincipal?),
        configFn: (AuthConfigContext.() -> Unit)? = null
    ) {
        authContext.setupAuthentication(cryptoKeys, responseProvider.getValue(this), userLookupFn)
        authConfigFn = configFn
    }

}