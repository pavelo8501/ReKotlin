package po.restwraptor

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import po.restwraptor.classes.ConfigContext

open class RestServer(
    private val config: (Application.() -> Unit)? = null
) {

    private val initialized: Boolean = false

    private lateinit var app: Application
    private lateinit var configuration : ConfigContext

    val host: String = "0.0.0.0"
    private set

    var port: Int = 0
    private set

    fun start(host: String = "0.0.0.0", port: Int = 8080,  wait: Boolean = true){
        startRest(host, port, wait)
    }

    protected fun startRest(host: String, port: Int,  wait: Boolean = true){
        embeddedServer(Netty, port, host){
            app = this
            if(!initialized){
                configuration = ConfigContext(app)
                config?.invoke(app)
            }
        }
    }

}