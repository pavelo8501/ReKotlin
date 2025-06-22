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


suspend fun RoutingContext.respondInternal(message: String, code: Int = 0) {
    call.respond(HttpStatusCode.InternalServerError, ApiResponse.withErrorMessage(message, code))
}

suspend fun RoutingContext.respondInternal(throwable : Throwable) {

    var code = 0
    if(throwable is DataException){
        code = throwable.source?.ordinal?:0
    }else{
        code =   HttpStatusCode.InternalServerError.value
    }
    call.respond(
        HttpStatusCode.InternalServerError,
        ApiResponse.withErrorMessage(throwable.message.toString(), code)
    )
}

suspend fun RoutingContext.respondUnauthorized(message: String, code: Int) {
    call.respondUnauthorized(message, code)
}
suspend fun ApplicationCall.respondUnauthorized(message: String, code: Int) {
    respond(HttpStatusCode.Unauthorized, ApiResponse.withErrorMessage(message, code))
}

private suspend fun badRequest(call : RoutingCall, message: String, code: Int = 0){
    call.respond(HttpStatusCode.BadRequest, ApiResponse.withErrorMessage(message, code))
}

suspend fun RoutingContext.respondBadRequest(message: String ){
    badRequest(call, message)
}

private suspend fun notFoundResponse(call : RoutingCall,  message: List<String> = emptyList<String>()){
    val additionalMessage = message.joinToString(", ")
    val message =  "Method: ${call.request.httpMethod} does not exist on path ${call.request.uri}. $additionalMessage"
    call.respond(HttpStatusCode.NotFound, ApiResponse.withErrorMessage(message, 0))
}

suspend fun  RoutingContext.respondNotFound(message: List<String> = emptyList<String>()){
    notFoundResponse(call, message)
}

suspend fun RoutingCall.respondNotFound( message: List<String> = emptyList<String>()) {
   val response =  notFoundResponse(this,message)
   this.respond(response)
}
