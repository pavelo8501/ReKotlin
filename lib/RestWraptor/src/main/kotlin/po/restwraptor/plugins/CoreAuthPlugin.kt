package po.restwraptor.plugins

import io.ktor.server.application.createApplicationPlugin

val CoreAuthPlugin = createApplicationPlugin("CoreAuthPlugin"){

    onCallReceive { call ->

        val headers =  call.request.headers

        val stop = 1
    }
}