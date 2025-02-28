package po.restwraptor.classes.convenience

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.RoutingContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import po.restwraptor.models.response.ApiResponse
import po.restwraptor.models.response.ServiceResponse


suspend fun RoutingContext.respondInternal(message: String) {
    call.respond<ServiceResponse>(
        HttpStatusCode.InternalServerError,
        ServiceResponse(message, HttpStatusCode.InternalServerError.value)
    )
}

suspend fun RoutingContext.respondInternal(ex : Throwable) {

    call.respond(
        HttpStatusCode.InternalServerError,
        ServiceResponse(ex.message.toString(), HttpStatusCode.InternalServerError.value)
    )
}

suspend fun RoutingContext.respondUnauthorized(message: String) {
    call.respond<ServiceResponse>(
        HttpStatusCode.Unauthorized,
        ServiceResponse(message, HttpStatusCode.Unauthorized.value)
    )
}