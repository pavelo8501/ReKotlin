package po.api.rest_service.server



import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.serialization.kotlinx.json.*

import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.*

import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.http.*
import io.ktor.server.routing.*


class ApiServer(
    private val configure: (Application.() -> Unit)? = null
) {
    private var host: String = "0.0.0.0"
    private var port: Int = 8080

    fun configureHost(host: String, port: Int): ApiServer {
        this.host = host
        this.port = port
        return this
    }

    fun start(wait: Boolean = true) {

        embeddedServer(Netty, port, host) {
            configure?.invoke(this)
            if (this.pluginOrNull(ContentNegotiation) != null) {
                //Can do something here if ContentNegotiation is already installed
            } else {
                install(ContentNegotiation) {
                    json()
                }
            }

            if (this.pluginOrNull(CORS) != null) {
                //Can do something here if CORS is already installed
            } else {
                install(CORS) {
                    run {
                        allowMethod(HttpMethod.Post)
                        allowHeader(HttpHeaders.ContentType)
                        allowCredentials = true
                        anyHost()
                    }
                }
            }

            routing {
                // Default routes
            }
        }.start(wait)
    }
}