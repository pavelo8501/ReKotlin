package po.restwraptor.classes.convenience

import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.Request
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
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

private suspend fun badRequest(call : RoutingCall,  message: List<String> = emptyList<String>()){
    val response =  ServiceResponse(
        "Data supplied can not be processed",
        HttpStatusCode.BadRequest.value)
    message.forEach { response.addLogRecord(it)}
    call.respond<ServiceResponse>(HttpStatusCode.BadRequest, response)
}

suspend fun  RoutingContext.respondBadRequest(message: List<String> = emptyList<String>()){
    badRequest(call, message)
}




private suspend fun notFoundResponse(call : RoutingCall,  message: List<String> = emptyList<String>()){
    val response =  ServiceResponse(
        "Method: ${call.request.httpMethod} does not exist on path ${call.request.uri}.",
        HttpStatusCode.NotFound.value)
    message.forEach { response.addLogRecord(it)}
    call.respond<ServiceResponse>(HttpStatusCode.NotFound, response)
}

suspend fun  RoutingContext.respondNotFound(message: List<String> = emptyList<String>()){
    notFoundResponse(call, message)
}

suspend fun RoutingCall.respondNotFound( message: List<String> = emptyList<String>()) {
   val response =  notFoundResponse(this,message)
   this.respond(response)
}
