package po.restwraptor

import io.ktor.server.application.Application
import io.ktor.server.application.ServerReady
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.EngineConnectorConfig
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import po.lognotify.TasksManaged
import po.lognotify.launchers.runTask
import po.lognotify.launchers.runTaskAsync
import po.misc.containers.LazyContainer
import po.misc.containers.lazyContainerOf
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.functions.registries.builders.notifierRegistryOf
import po.misc.types.getOrThrow
import po.restwraptor.scope.ConfigContext

import po.restwraptor.enums.EnvironmentType
import po.restwraptor.exceptions.ConfigurationException
import po.restwraptor.exceptions.ExceptionCodes
import po.restwraptor.interfaces.WraptorHandler
import po.restwraptor.interfaces.WraptorResponse
import po.restwraptor.models.configuration.WraptorConfig
import po.restwraptor.models.info.WraptorStatus
import po.restwraptor.models.server.WraptorRoute

val RestWrapTorKey = AttributeKey<RestWrapTor>("RestWrapTorInstance")


object RestWraptorServer:RestWrapTor() {

}

fun configureWraptor(builder : (ConfigContext.() -> Unit)){
    RestWraptorServer.config(RestWraptorServer, builder)
}

fun configureApplication(builder : (Application.() -> Unit)){
    RestWraptorServer.configureApplication(builder)
}


fun runWraptor(
    host: String = "0.0.0.0",
    port: Int = 8080,
    wait: Boolean = true,
    onStarted : ((WraptorHandler)-> Unit)? = null
): Unit = RestWraptorServer.start(host, port, wait, onStarted)



/**
 * **RestWrapTor** - A wrapper for managing Ktor applications with configurable settings and lifecycle hooks.
 * This class simplifies Ktor application initialization, configuration, and server management.
 * @property appConfigFn (Optional) Function to apply additional configurations to the application.
 */
open class RestWrapTor(
    internal val builder : (ConfigContext.() -> Unit)? = null
): WraptorHandler, TasksManaged {

    override val identity: CTXIdentity<RestWrapTor> = asIdentity()

    /**
     * Stores the **unique hash** of the application instance.
     * Used to verify if the application instance remains the same.
     */
    var appHash: Int = 0
        private set

    internal val configContextBacking: LazyContainer<ConfigContext> = lazyContainerOf()
    internal val applicationRegistry = notifierRegistryOf<Application>()
    internal val configRegistry = notifierRegistryOf<ConfigContext>()
    internal var preSavedApplication: Application? = null

    /** The embedded Ktor server instance. */
    private lateinit var embeddedServer: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

    final override val wraptorRoutes: List<WraptorRoute> get() = getRoutes()
    final override val rootPath: String get() = configContextBacking.value?.application?.rootPath ?: "N/A"


    val connectors: MutableList<String> = mutableListOf<String>()

    /**
     * Indicates whether the application has been initialized.
     * - `false`: The application is uninitialized.
     * - `true`: The application has been set up and is ready for use.
     */
    var initialized: Boolean = false
        private set

    /** Internal configuration settings for `RestWrapTor`. */
    internal var wrapConfig = WraptorConfig()

    private val authConfig get() = wrapConfig.authConfig
    private val apiConfig get() = wrapConfig.apiConfig

    /** Context for handling application configuration. */
    // private val configContext : ConfigContext = ConfigContext(this, wrapConfig)


//    private var hasCoreContext : CoreContext? = null
//    private val coreContext : CoreContext
//        get(){ return hasCoreContext?:throw ConfigurationException("Core context accessed before init", ExceptionCodes.VALUE_IS_NULL, null)}


    /** The attribute key used to store this instance inside the `Application.attributes`. */
    lateinit var thisKey: AttributeKey<RestWrapTor>

    /** The host address for the server. Defaults to `0.0.0.0`. */
    private var host: String = "0.0.0.0"

    /** The port number for the server. Defaults to `8080`. */
    private var port: Int = 8080

    /** Determines if the server should block the main thread. Defaults to `true`. */
    private var wait: Boolean = true

    /** Callback function triggered when the server starts successfully. */
    private var onServerStartedCallback: ((WraptorHandler) -> Unit)? = null

    private val serviceScope = CoroutineScope(Dispatchers.IO + CoroutineName("WrapTor service"))

    init {
        builder?.let {
            configRegistry.subscribe(this, it)
        }
    }

    private suspend fun engineStarted(engine: ApplicationEngine) = runTaskAsync("EngineStarted") {
        engine.resolvedConnectors().forEach {
            connectors.add("${it.type}|${it.host}:${it.port}")
        }
    }

    /**
     * Registers this `RestWrapTor` instance in `Application.attributes`.
     */
    private fun registerSelf(application: Application): AttributeKey<RestWrapTor>? {
        if (!application.attributes.contains(RestWrapTorKey)) {
            application.attributes.put(RestWrapTorKey, this)
            thisKey = RestWrapTorKey
            return thisKey
        } else {
            return null
        }
    }

    /**
     * Retrieves a list of all registered routes in the application.
     * @return A list of `WraptorRoute` objects representing the registered routes.
     */

    private var getRoutesCallback: (((List<WraptorRoute>) -> Unit)?) = null

    private fun applyConfig(app: Application) {
        val config = ConfigContext(this, app)
        applicationRegistry.trigger(config.application)
        configRegistry.trigger(config)
        config.initialize()
        configContextBacking.provideValue(config)
    }

    private fun setupConfig(app: Application) = runTask("Configuration") {
        app.monitor.subscribe(ServerReady) {
            onServerStartedCallback?.invoke(this)
        }
        registerSelf(app).getOrThrow(this) { _ ->
            ConfigurationException(
                "RestWrapTor Registration inside Application failed",
                ExceptionCodes.KEY_REGISTRATION
            )
        }
        applyConfig(app)
        appHash = System.identityHashCode(app)
        notify("App hash of the WrapTor configured app $appHash")
        initialized = true
        getRoutesCallback?.invoke(getRoutes())
    }

    /**
     * Launches the Ktor embedded server.
     *
     * - Configures the application before starting the server.
     * - Registers event listeners for `ApplicationStarted` and `ServerReady` events.
     *
     * @param wait If `true`, the server will block execution until manually stopped.
     */
    private fun launchRest(wait: Boolean = true) {
        embeddedServer = embeddedServer(Netty, port, host) {
            setupConfig(this)
        }
        embeddedServer.start(wait)
    }


    /**
     * Used for Test environments
     */
    internal fun configWithApp(app: Application, builder: ConfigContext.() -> Unit) {
        preSavedApplication = app
        configRegistry.subscribe(this, builder)
    }

    internal fun config(callingContext: Any, builder: ConfigContext.() -> Unit) {
        configRegistry.subscribe(callingContext, builder)
    }

    override fun getRoutes(callback: ((List<WraptorRoute>) -> Unit)?): List<WraptorRoute> {
        getRoutesCallback = callback
        val routes = configContextBacking.getValue(this).coreContext.getWraptorRoutes()
        callback?.invoke(routes)
        return routes
    }

    override fun getConnectors(callback: (List<EngineConnectorConfig>) -> Unit) {
        callback.invoke(embeddedServer.engineConfig.connectors.toList())
    }

    fun setWraptorResponse(provider:()-> WraptorResponse<*>){
        configContextBacking.requestValue(this){config->
            config.registerResponseProvider(provider)
        }
    }

    fun configureApplication(builder: Application.() -> Unit) {
        applicationRegistry.subscribe(this, builder)
    }

    fun getRoutes(): List<WraptorRoute> {
        return configContextBacking.getValue(this).coreContext.getWraptorRoutes()
    }


    /**
     * Retrieves the current `Application` instance.
     * @return The `Application` instance associated with this `RestWrapTor`.
     */
    fun getApp(): Application {
        return configContextBacking.getValue(this).application
    }

    override fun stop(gracePeriod: Long) {
        configContextBacking.getValue(this).application.engine.stop(gracePeriod)
    }

    /**
     * Starts the Ktor server with the specified host and port.
     *
     * @param host The host address the server will bind to (default: `"0.0.0.0"`).
     * @param port The port number the server will listen on (default: `8080`).
     * @param wait If `true`, the server will block execution until manually stopped.
     *
     * @throws IllegalStateException If the server is already initialized or improperly configured.
     */
    fun start(
        host: String = "0.0.0.0",
        port: Int = 8080,
        wait: Boolean = true,
        onStarted: ((WraptorHandler) -> Unit)? = null
    ) {
        this.onServerStartedCallback = onStarted
        this.host = host.ifBlank { this.host }
        this.port = port
        this.wait = wait
        launchRest(wait)
    }

    fun status(): WraptorStatus {
        val activeConnectors = mutableListOf<String>()
        serviceScope.async {
            val activeConnectors = mutableListOf<String>()
            embeddedServer.engine.resolvedConnectors().forEach {
                activeConnectors.add("${it.type}|${it.host}:${it.port}")
            }
        }
        val thisApp = getApp()
        val isProduction = this.wrapConfig.apiConfig.environment == EnvironmentType.PROD
        val allRoutes = getRoutes()

        return WraptorStatus(
            true,
            activeConnectors,
            thisApp.rootPath,
            isProduction,
            emptyList(),
            emptyList(),
            allRoutes.filter { it.isSecured == false },
            allRoutes.filter { it.isSecured == true },
        )
    }
}