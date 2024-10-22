package po.api.rest_service.server


import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*

class AuthenticationException(message: String) : RuntimeException(message)
class AuthorizationException(message: String) : RuntimeException(message)

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<AuthenticationException> { call, reason ->
            call.respond(HttpStatusCode.Unauthorized, reason.message ?: "Unauthorized")
        }
        exception<AuthorizationException> { call, reason ->
            call.respond(HttpStatusCode.Forbidden, reason.message ?: "Forbidden")
        }
        exception<Throwable> { call, reason ->
            call.respond(HttpStatusCode.InternalServerError, reason.message ?: "Internal Server Error")
        }
    }
}

