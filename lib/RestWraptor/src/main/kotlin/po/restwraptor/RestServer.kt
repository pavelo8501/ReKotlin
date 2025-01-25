package po.restwraptor

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import po.restwraptor.classes.ConfigContext
import po.restwraptor.models.configuration.ApiConfig

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

    fun start(host: String = "0.0.0.0", port: Int = 8080,  wait: Boolean = true){
        this.host = host.ifBlank { this.host }
        this.port = port
        this.wait = wait
        configRest(wait)
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

    fun getApp(): Application{
        return application
    }

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