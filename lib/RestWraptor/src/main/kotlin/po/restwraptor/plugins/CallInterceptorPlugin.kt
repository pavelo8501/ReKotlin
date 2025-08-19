package po.restwraptor.plugins

import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.request.uri
import io.ktor.server.response.respondRedirect
import po.auth.authentication.jwt.JWTService
import po.auth.getOrNewSession
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.restwraptor.calls.callerInfo
import po.restwraptor.session.sessionFromAttributes
import po.restwraptor.session.toAttributes

val CallInterceptorPlugin = createApplicationPlugin(
    name = "CallInterceptorPlugin",
    createConfiguration =  ::PluginConfiguration
){

    pluginConfig.apply {

        onCall { call ->
            try{
                val info = call.callerInfo()
                val session =  getOrNewSession(info)
                session.onRoundTripStart(info.route)
                call.toAttributes(session)

                val headers = call.request.headers
                headers.forEach { name, values ->  println("${name} : ${values}") }
                val uri = call.request.uri
                if (uri.endsWith("/") && uri != "/") {
                    call.respondRedirect(uri.removeSuffix("/"))
                    return@onCall
                }
            } catch (e: Exception) {
                val a = e.message
            }
        }

       onCallRespond { call ->
           val info = call.callerInfo()
           call.sessionFromAttributes()?.let {session->

               session.onRoundTripEnd(info.route)
           }
       }

//        on(ResponseSent) { call ->
//
//        }
    }
}

class PluginConfiguration {
    var service: JWTService? = null

    fun injectService(jwtService: JWTService){
        service = jwtService
    }
}