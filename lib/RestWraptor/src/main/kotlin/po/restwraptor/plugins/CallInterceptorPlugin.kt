package po.restwraptor.plugins

import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.plugins.origin

val CallInterceptorPlugin = createApplicationPlugin(name = "CallInterceptorPlugin") {

    onCall { call ->
        call.request.origin.apply {
            println("Request URL: $scheme://$localHost:$localPort$uri")
        }
    }

}