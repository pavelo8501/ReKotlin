package po.restwraptor.classes.convenience

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import po.restwraptor.models.response.ServiceResponse


suspend infix fun RoutingContext.respondInternal(message: String) {
    call.respond<ServiceResponse>(
        HttpStatusCode.InternalServerError,
        ServiceResponse(message, HttpStatusCode.InternalServerError.value)
    )
}

suspend infix fun RoutingContext.respondInternal(ex : Throwable) {
    call.respond<ServiceResponse>(
        HttpStatusCode.InternalServerError,
        ServiceResponse(ex.message.toString(), HttpStatusCode.InternalServerError.value)
    )
}

suspend infix fun RoutingContext.respondUnauthorized(message: String) {
    call.respond<ServiceResponse>(
        HttpStatusCode.Unauthorized,
        ServiceResponse(message, HttpStatusCode.Unauthorized.value)
    )
}