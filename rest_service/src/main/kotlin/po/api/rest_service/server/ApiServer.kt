package po.api.rest_service.server

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*


class ApiServer {
    private var host: String = "0.0.0.0"
    private var port: Int = 8080
    private var module: Application.() -> Unit = {}

    fun configureHost(host: String, port: Int): ApiServer {
        this.host = host
        this.port = port
        return this
    }

    fun configureModule(module: Application.() -> Unit): ApiServer {
        this.module = module
        return this
    }

    fun start(wait: Boolean = true): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {
        return embeddedServer(Netty, host = host, port = port, module = module).start(wait = wait)
    }
}