package po.restwraptor

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import po.restwraptor.classes.ConfigContext
import po.restwraptor.models.configuration.ApiConfig

/**
 * A customizable REST server wrapper for Ktor applications, providing
 * configuration and initialization utilities.
 * @property app The Ktor application instance to initialize with. If null, the server will require manual setup.
 * @property configFn An optional configuration function to set up the server context.
 */
open class RestServer(
    app : Application? = null,
    private val configFn: (ConfigContext.() -> Unit)? = null
) {

    private var initialized: Boolean = false
    private lateinit var application: Application
    private lateinit var configContext : ConfigContext

    var host: String = "0.0.0.0"
    private set

    var port: Int = 0
    private set

    var wait: Boolean = true
    private set

    init {
        if(app!=null){
            setupConfig(app)
        }
    }

    private fun setupConfig(app: Application){
        if (!initialized) {
            configContext = ConfigContext(app)
            if(configFn!= null){
                configContext.configFn()
            }
            application = configContext.initialize()
            initialized = true
        }
    }

    /**
     * Starts the REST server with the specified host, port, and blocking behavior.
     * @param host The host address the server will bind to. Defaults to "0.0.0.0".
     * @param port The port the server will listen on. Defaults to 8080.
     * @param wait If true, the server will block the current thread and wait. Defaults to true.
     *
     * @throws IllegalStateException if the server is already initialized or improperly configured.
     */
    fun start(host: String = "0.0.0.0", port: Int = 8080,  wait: Boolean = true){
        this.host = host.ifBlank { this.host }
        this.port = port
        this.wait = wait
        configRest(wait)
    }

    /**
     * Retrieves the initialized Ktor application instance.
     * @return The Ktor application instance.
     * @throws UninitializedPropertyAccessException if the application has not been initialized.
     */
    fun getApp(): Application{
        return application
    }

    /**
     * Retrieves the server's API configuration.
     * @return An [ApiConfig] object representing the server's configuration.
     * @throws UninitializedPropertyAccessException if the configuration context has not been initialized.
     */
    fun getConfig(): ApiConfig{
        return configContext.apiConfig
    }

    protected fun configRest(wait: Boolean = true): Application {
        embeddedServer(Netty, port, host){
            setupConfig(this)
        }.start(wait)
        return application
    }
}