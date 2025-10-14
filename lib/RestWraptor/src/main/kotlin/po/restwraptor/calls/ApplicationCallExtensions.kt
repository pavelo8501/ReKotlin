package po.restwraptor.calls

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import io.ktor.server.request.uri
import po.restwraptor.enums.WraptorHeaders



fun ApplicationCall.callerInfo(
    headersOfInterest: List<String> = listOf(WraptorHeaders.XAuthToken.toString(), WraptorHeaders.XAuthToken.toString())
): CallerInfo {

    val ip = request.header("X-Forwarded-For") ?: request.local.remoteHost
    val route = request.uri
    val collectedHeaders = headersOfInterest
        .mapNotNull { name -> request.headers[name]?.let { name to it } }
        .toMap()

    return CallerInfo(
        ip = ip,
        userAgent = request.headers["User-Agent"]?:"N/A",
        route = route,
        headers = collectedHeaders
    )
}