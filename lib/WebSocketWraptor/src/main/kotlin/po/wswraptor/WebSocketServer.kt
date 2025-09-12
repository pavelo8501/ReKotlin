package po.wswraptor

import io.ktor.server.application.Application
import io.ktor.server.routing.Routing
import io.ktor.server.websocket.webSocket
import po.restwraptor.RestWrapTor
import po.wswraptor.classes.WSConfigContext
import po.wswraptor.models.configuration.WsApiConfig
import po.wswraptor.routing.WSRoute
import po.wswraptor.services.ConnectionService

inline fun Routing.webSocket(
        path: String,
        resource: String,
       noinline handler: suspend WSRoute.() -> Unit){

        webSocket(path) {
            val rootRoute =  WSRoute(path, resource, "", this)
            rootRoute
            rootRoute.handler()
        }

    }

class WebSocketServer(
    app : Application? = null,
    private val configFn: (WSConfigContext.() -> Unit)? = null) {

    private var initialized: Boolean = false
    private lateinit var application: Application
    private lateinit var configContext : WSConfigContext

    private lateinit var server : RestWrapTor

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

    fun configure(block:  WSConfigContext.()-> Unit){
        configContext.block()
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
      //  server = RestWrapTor(application)
        server.start(host, port, wait)
        return application
    }


    companion object{
       private val routBuffer =  mutableMapOf<String, WSRoute>()

        fun addRout(path : String, rout : WSRoute){
            routBuffer.putIfAbsent(path, rout)
        }
    }
}