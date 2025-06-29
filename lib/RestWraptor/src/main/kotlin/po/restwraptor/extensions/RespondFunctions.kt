package po.restwraptor.extensions

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.RoutingContext
import po.restwraptor.exceptions.DataException
import po.restwraptor.models.response.ApiResponse
import io.ktor.server.response.respond
import kotlinx.serialization.json.Json

fun errorResponse(message: String, code: Int):ApiResponse<String>{
   return ApiResponse("").setErrorMessage(message, code)
}
suspend fun RoutingContext.respondInternal(message: String, code: Int = HttpStatusCode.InternalServerError.value) {
    call.respond(HttpStatusCode.InternalServerError, errorResponse(message, code))
}
suspend fun RoutingCall.respondInternal(throwable : Throwable) {
    respond(HttpStatusCode.InternalServerError, errorResponse(throwable.message.toString(), HttpStatusCode.InternalServerError.value))
}
suspend fun RoutingCall.respondInternal(message: String, exceptionCode: Int = HttpStatusCode.InternalServerError.value) {
    respond(HttpStatusCode.InternalServerError, errorResponse(message, exceptionCode))
}

suspend fun RoutingContext.respondUnauthorized(message: String, code: Int) {
    call.respondUnauthorized(message, code)
}
suspend fun ApplicationCall.respondUnauthorized(message: String, code: Int) {
    respond(HttpStatusCode.Unauthorized, ApiResponse.withErrorMessage(message, code))
}

suspend fun RoutingCall.respondBadRequest(message: String, exceptionCode: Int = HttpStatusCode.BadRequest.value) {
    respond(HttpStatusCode.BadRequest, errorResponse(message, exceptionCode))
}
suspend fun RoutingContext.respondBadRequest(message: String, exceptionCode: Int = HttpStatusCode.BadRequest.value){
    call.respond(HttpStatusCode.BadRequest, errorResponse(message, exceptionCode))
}

suspend fun  RoutingContext.respondNotFound(message: List<String> = emptyList<String>()){
    val additionalMessage = message.joinToString(", ")
    val message =  "Method: ${call.request.httpMethod} does not exist on path ${call.request.uri}. $additionalMessage"
    call.respond(HttpStatusCode.NotFound,  errorResponse(message, HttpStatusCode.NotFound.value))
}
suspend fun RoutingCall.respondNotFound( message: List<String> = emptyList<String>()) {
    val additionalMessage = message.joinToString(", ")
    val message =  "Method: ${request.httpMethod} does not exist on path ${request.uri}. $additionalMessage"
    respond(HttpStatusCode.NotFound, errorResponse(message, HttpStatusCode.NotFound.value))
}
