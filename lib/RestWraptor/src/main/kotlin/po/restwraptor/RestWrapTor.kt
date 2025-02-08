package po.restwraptor

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ServerReady
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.util.AttributeKey
import po.restwraptor.classes.ConfigContext
import po.restwraptor.classes.CoreContext
import po.restwraptor.models.configuration.ApiConfig
import po.restwraptor.models.configuration.WraptorConfig
import po.restwraptor.models.server.WraptorRoute

val RestWrapTorKey = AttributeKey<RestWrapTor>("RestWrapTorInstance")

/**
 * **RestWrapTor** - A wrapper for managing Ktor applications with configurable settings and lifecycle hooks.
 * This class simplifies Ktor application initialization, configuration, and server management.
 * @property appConfigFn (Optional) Function to apply additional configurations to the application.
 */
class RestWrapTor(
    private val appConfigFn : (ConfigContext.() -> Unit)? = null,
) {
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
    private val coreContext : CoreContext by lazy {  CoreContext(application) }

    /** The attribute key used to store this instance inside the `Application.attributes`. */
    lateinit var thisKey :  AttributeKey<RestWrapTor>

    /** The host address for the server. Defaults to `0.0.0.0`. */
    private var host: String = "0.0.0.0"

    /** The port number for the server. Defaults to `8080`. */
    private var port: Int = 0

    /** Determines if the server should block the main thread. Defaults to `true`. */
    private var wait: Boolean = true

    /** Callback function triggered when the server starts successfully. */
    private var onServerStartedCallback : ((EmbeddedServer<*,*>) -> Unit)? = null

    /**
     * Registers a callback to be executed when the server successfully starts.
     * @param callback A function that receives the `EmbeddedServer` instance after startup.
     */
    fun onServerStarted(callback : (EmbeddedServer<*,*>)-> Unit ){
        onServerStartedCallback = callback
    }

    /**
     * Applies custom configuration to the application.
     * @param configFn The function that applies configuration settings.
     */
    internal fun  applyConfig(configFn : ConfigContext.()-> Unit){
        configContext.configFn()
    }

    /**
     * Registers this `RestWrapTor` instance in `Application.attributes`.
     */
    private fun registerSelf(){
        if (!application.attributes.contains(RestWrapTorKey)) {
            application.attributes.put(RestWrapTorKey, this)
            thisKey = RestWrapTorKey
        }
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
    private fun setupConfig(app: Application){
        if(!::application.isInitialized){
            application = app
            appConfigFn?.let {
                configContext.it()
            }
            configContext.initialize()
        }
        appHash = System.identityHashCode(application)
        println("Hash of the wraptor app ${System.identityHashCode(application)}")
        registerSelf()
        initialized = true
    }

    /**
     * Allows an **already existing application instance** to be used instead of creating a new one.
     * @param app The preconfigured `Application` instance.
     */
    fun usePreconfiguredApp(app : Application){
        setupConfig(app)
    }

    /** Hook executed after the application starts. */
    private fun afterApplicationStart(){

    }

    /** Hook executed after the server starts successfully. */
    private fun afterServerStart(){
        onServerStartedCallback?.invoke(embeddedServer)
    }

    /**
     * Retrieves a list of all registered routes in the application.
     * @return A list of `WraptorRoute` objects representing the registered routes.
     */
    fun getRoutes():List<WraptorRoute>{
        println("Hash on getRoutes ${System.identityHashCode(application)}")
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
     * @return An `ApiConfig` object representing the server's configuration, or `null` if uninitialized.
     */
    fun getConfig(): ApiConfig?{
        if(initialized == false){
            return null
        }else{
            return configContext.apiConfig
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
    private fun launchRest(wait: Boolean = true){
        embeddedServer =  embeddedServer(Netty, port, host){
            println("Hash before appBuilderFn invoked ${System.identityHashCode(this)}")
            setupConfig(this)
            monitor.subscribe(ApplicationStarted) { afterApplicationStart() }
            monitor.subscribe(ServerReady) { afterServerStart() }
        }
        embeddedServer.start(wait)
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
    fun start(host: String = "0.0.0.0", port: Int = 8080,  wait: Boolean = true){
        this.host = host.ifBlank { this.host }
        this.port = port
        this.wait = wait
        launchRest(wait)
    }

}