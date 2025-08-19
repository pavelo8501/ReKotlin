package po.restwraptor.extensions

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.RoutingContext
import po.misc.exceptions.throwableToText
import po.restwraptor.interfaces.WraptorResponse


suspend fun RoutingContext.respondInternal(
    message: String,
    code: Int = HttpStatusCode.InternalServerError.value,
    responseProvider:()->WraptorResponse<*>
) = call.respondInternal(message, code, responseProvider)

suspend fun RoutingCall.respondInternal(
    throwable : Throwable,
    responseProvider:()->WraptorResponse<*>
) {
    val payload = responseProvider.invoke().setErrorMessage(throwable.throwableToText(), HttpStatusCode.InternalServerError.value)
    respond(HttpStatusCode.InternalServerError, payload)
}

suspend fun RoutingCall.respondInternal(
    message: String,
    exceptionCode: Int = HttpStatusCode.InternalServerError.value,
    responseProvider:()->WraptorResponse<*>
) {
    val payload = responseProvider.invoke().setErrorMessage(message, exceptionCode)
    respond(HttpStatusCode.InternalServerError, payload)
}

suspend fun RoutingContext.respondUnauthorized(
    message: String,
    code: Int,
    responseProvider:()->WraptorResponse<*>
) =  call.respondUnauthorized(message, code, responseProvider)

suspend fun ApplicationCall.respondUnauthorized(
    message: String,
    code: Int,
    responseProvider:()->WraptorResponse<*>
) {
    val payload = responseProvider.invoke().setErrorMessage(message, code)
    respond(HttpStatusCode.Unauthorized, payload)
}

suspend fun RoutingCall.respondBadRequest(
    message: String,
    exceptionCode: Int = HttpStatusCode.BadRequest.value,
    responseProvider:()->WraptorResponse<*>
) {
    val payload = responseProvider.invoke().setErrorMessage(message, exceptionCode)
    respond(HttpStatusCode.BadRequest, payload)
}
suspend fun RoutingContext.respondBadRequest(
    message: String,
    exceptionCode: Int = HttpStatusCode.BadRequest.value,
    responseProvider:()->WraptorResponse<*>
) = call.respondBadRequest(message, exceptionCode, responseProvider)

suspend fun RoutingCall.respondNotFound(
    message: List<String> = emptyList<String>(),
    responseProvider:()->WraptorResponse<*>
) {
    val additionalMessage = message.joinToString(", ")
    val message =  "Method: ${request.httpMethod} does not exist on path ${request.uri}. $additionalMessage"
    responseProvider.invoke().setErrorMessage(message, HttpStatusCode.NotFound.value )
    respond(HttpStatusCode.NotFound, responseProvider)
}

suspend fun  RoutingContext.respondNotFound(
    message: List<String> = emptyList<String>(),
    responseProvider:()->WraptorResponse<*>
) = call.respondNotFound(message, responseProvider)

