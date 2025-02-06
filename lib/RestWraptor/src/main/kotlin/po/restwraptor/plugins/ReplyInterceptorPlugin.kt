package po.restwraptor.plugins

import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.ResponseSent
import po.restwraptor.security.JWTService

val ReplyInterceptorPlugin = createApplicationPlugin(
    name = "CallInterceptorPlugin",
    createConfiguration =  ::PluginConfiguration
    ) {

    pluginConfig.apply {
        on(ResponseSent) { call ->
            service?.let {jwtService->
                call.request.headers["Authorization"]?.let {value->
                    jwtService.checkExpiration(value) {token->
                        if(token!=null){
                            call.response.headers.append("Authorization", "Bearer $token")
                        }
                    }
                }
            }
        }
    }
}

class PluginConfiguration {
    var service: JWTService? = null

    fun injectService(jwtService: JWTService){
        service = jwtService
    }
}