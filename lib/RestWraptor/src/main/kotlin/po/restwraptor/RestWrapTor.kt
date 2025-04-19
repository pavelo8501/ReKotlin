package po.restwraptor

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ServerReady
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import po.lognotify.TasksManaged
import po.lognotify.extensions.startTaskAsync
import po.lognotify.extensions.subTask
import po.restwraptor.scope.ConfigContext
import po.restwraptor.scope.CoreContext
import po.restwraptor.enums.EnvironmentType
import po.restwraptor.exceptions.ExceptionCodes
import po.restwraptor.extensions.getOrConfigurationEx
import po.restwraptor.interfaces.WraptorHandler
import po.restwraptor.models.configuration.ApiConfig
import po.restwraptor.models.configuration.WraptorConfig
import po.restwraptor.models.info.WraptorStatus
import po.restwraptor.models.server.WraptorRoute

val RestWrapTorKey = AttributeKey<RestWrapTor>("RestWrapTorInstance")

/**
 * **RestWrapTor** - A wrapper for managing Ktor applications with configurable settings and lifecycle hooks.
 * This class simplifies Ktor application initialization, configuration, and server management.
 * @property appConfigFn (Optional) Function to apply additional configurations to the application.
 */
class RestWrapTor(
    private val appConfigFn : ( suspend ConfigContext.() -> Unit)? = null,
): WraptorHandler, TasksManaged
{

    private val personalName : String = "RestWrapTor"



    /**
     * Stores the **unique hash** of the application instance.
     * Used to verify if the application instance remains the same.
     */
    var appHash : Int  =  0
        private set
    /** The Ktor application instance managed by this wrapper. */
    internal lateinit var application: Application

    /** The embedded Ktor server instance. */
    private lateinit var  embeddedServer : EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

    val connectors: MutableList<String> = mutableListOf<String>()

    /**
     * Indicates whether the application has been initialized.
     * - `false`: The application is uninitialized.
     * - `true`: The application has been set up and is ready for use.
     */
    var initialized: Boolean = false
        private set

    /** Internal configuration settings for `RestWrapTor`. */
    private var wrapConfig  = WraptorConfig()

    /** Context for handling application configuration. */
    private val configContext : ConfigContext = ConfigContext(this, wrapConfig)

    /** Core context for managing routes and API structure. */
    private val coreContext : CoreContext by lazy {  CoreContext(application, this) }

    /** The attribute key used to store this instance inside the `Application.attributes`. */
    lateinit var thisKey :  AttributeKey<RestWrapTor>

    /** The host address for the server. Defaults to `0.0.0.0`. */
    private var host: String = "0.0.0.0"

    /** The port number for the server. Defaults to `8080`. */
    private var port: Int = 8080

    /** Determines if the server should block the main thread. Defaults to `true`. */
    private var wait: Boolean = true

    /** Callback function triggered when the server starts successfully. */
    private var onServerStartedCallback : ((WraptorHandler)-> Unit)?  = null

    private val serviceScope = CoroutineScope(Dispatchers.IO + CoroutineName("WrapTor service"))

    private fun engineStarted(engine: ApplicationEngine){
        startTaskAsync("EngineStarted", personalName){
            engine.resolvedConnectors().forEach {
                connectors.add("${it.type}|${it.host}:${it.port}")
            }
        }
    }

    /**
     * Applies custom configuration to the application.
     * @param configFn The function that applies configuration settings.
     */
    internal fun applyConfig(configFn : suspend ConfigContext.()-> Unit){
        startTaskAsync("Applying Configuration", personalName){
            configContext.configFn()
        }
    }

    /**
     * Registers this `RestWrapTor` instance in `Application.attributes`.
     */
    private fun registerSelf(): AttributeKey<RestWrapTor>?{

        if (!application.attributes.contains(RestWrapTorKey)) {
            application.attributes.put(RestWrapTorKey, this)
            thisKey = RestWrapTorKey
            return thisKey
        }else{
            return null
        }
    }

    /** Hook executed after the server starts successfully. */
    fun  afterServerStart(){
        onServerStartedCallback?.invoke(this)
    }

    /**
     * Retrieves a list of all registered routes in the application.
     * @return A list of `WraptorRoute` objects representing the registered routes.
     */
    override fun getRoutes():List<WraptorRoute>{
       // println("Hash on getRoutes ${System.identityHashCode(application)}")
        return coreContext.getWraptorRoutes()
    }


    /**
     * Retrieves the current `Application` instance.
     * @return The `Application` instance associated with this `RestWrapTor`.
     */
    fun getApp(): Application{
        return application
    }

    /**
     * Retrieves the server's API configuration.
     * @return An `ApiConfig` object representing the server's configuration, or default if uninitialized.
     */
    fun getConfig(): ApiConfig{
        return configContext.apiConfig
    }

    /**
     * Sets up the configuration for the Ktor application.
     *
     * - If the application is not initialized, it sets up configurations and assigns the instance.
     * - Applies additional configurations if provided via `appConfigFn`.
     * - Initializes the `ConfigContext`.
     *
     * @param app The `Application` instance to configure.
     */
    private  fun setupConfig(app : Application) {

        startTaskAsync("Configuration", "RestWrapTor"){handler->
            application = app
            handler.info("App hash before appBuilderFn invoked ${System.identityHashCode(this)}")
            application.monitor.subscribe(ServerReady) {
                afterServerStart()
            }
//            application.monitor.subscribe(ApplicationStarted){
//                engineStarted(it.engine)
//            }
            registerSelf().getOrConfigurationEx("RestWrapTor Registration inside Application failed", ExceptionCodes.KEY_REGISTRATION)
            appConfigFn?.let{fn->
                configContext.fn()
            }
            configContext.initialize()
            appHash = System.identityHashCode(application)
            handler.info("App hash of the WrapTor configured app $appHash")
            initialized = true
        }
    }

    /**
     * Launches the Ktor embedded server.
     *
     * - Configures the application before starting the server.
     * - Registers event listeners for `ApplicationStarted` and `ServerReady` events.
     *
     * @param wait If `true`, the server will block execution until manually stopped.
     */
    private fun  launchRest(wait: Boolean = true){
        embeddedServer = embeddedServer(Netty, port, host){
            setupConfig(this)
        }
        embeddedServer.start(wait)
    }

    override fun stop(gracePeriod: Long){
        application.engine.stop(gracePeriod)
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
    fun start(host: String = "0.0.0.0", port: Int = 8080,  wait: Boolean = true, onStarted : ((WraptorHandler)-> Unit) ? = null){
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
        val isProduction = this.wrapConfig.enviromnent == EnvironmentType.PROD
        val allRoutes = coreContext.getWraptorRoutes()

        return  WraptorStatus(
            true,
            activeConnectors,
            thisApp.rootPath,
            isProduction,
            emptyList(),
            emptyList(),
            allRoutes.filter { it.isSecured  == false },
            allRoutes.filter { it.isSecured  == true },
            )
    }

}