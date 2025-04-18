package po.restwraptor.routes

import io.ktor.server.request.receive
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import po.auth.authentication.extensions.validateCredentials
import po.restwraptor.extensions.toUrl
import po.restwraptor.models.request.LoginRequest

fun configureAuthRoutes(routing: Routing, baseURL: String){

    routing.apply {
        val loginUrl =  toUrl(baseURL, "login")
        post(loginUrl){
            val credentials = call.receive<LoginRequest>()
            validateCredentials(credentials.login, credentials.password)

        }

    }

}