package po.wswraptor

import io.ktor.server.application.Application
import io.ktor.server.routing.Routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import po.restwraptor.RestServer
import po.wswraptor.classes.WSConfigContext
import po.wswraptor.models.configuration.WsApiConfig
import po.wswraptor.services.ConnectionService

    suspend inline fun Routing.webSocket(
        path: String,
        resource: String,
        handler: suspend DefaultWebSocketServerSession.() -> Unit){

    }


class WebSocketServer(
    app : Application? = null,
    private val configFn: (WSConfigContext.() -> Unit)? = null) {

    private var initialized: Boolean = false
    private lateinit var application: Application
    private lateinit var configContext : WSConfigContext

    private lateinit var server : RestServer

    var host: String = "0.0.0.0"
        private set
    var port: Int = 0
        private set
    var wait: Boolean = true
        private set

    val connectionService = ConnectionService()

    init {
        if(app!=null){
            setupConfig(app)
        }
    }

    private fun setupConfig(app: Application){
        if (!initialized) {
            configContext = WSConfigContext(app)
            if(configFn!= null){
                configContext.configFn()
            }
            application = configContext.initializeWs()
            initialized = true
        }
    }

    fun start(host: String, port: Int, wait: Boolean){
        this.host = host.ifBlank { this.host }
        this.port = port
        this.wait = wait
        configWS()
    }
    fun getConfig(): WsApiConfig{
        return configContext.wsApiConfig
    }

    private fun configWS(): Application {
        server = RestServer(application)
        server.start(host, port, wait)
        return application
    }

}