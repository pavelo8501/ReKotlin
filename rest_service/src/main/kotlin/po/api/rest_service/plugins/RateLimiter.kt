package po.api.rest_service.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.call
import io.ktor.server.plugins.origin
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.util.AttributeKey
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import po.api.rest_service.models.ApiResponse
import kotlin.time.TimeSource


class RateLimiterConfig {
    var requestsPerMinute: Int = 60
}

class RateLimiter(private val config: RateLimiterConfig) {

    private val requestCounts = mutableMapOf<String, MutableList<Long>>()
    private val mutex = Mutex()

    companion object Plugin : BaseApplicationPlugin<Application, RateLimiterConfig, RateLimiter> {
        override val key = AttributeKey<RateLimiter>("RateLimiter")

        override fun install(pipeline: Application, configure: RateLimiterConfig.() -> Unit): RateLimiter {
            val config = RateLimiterConfig().apply(configure)
            val rateLimiter = RateLimiter(config)

            pipeline.intercept(ApplicationCallPipeline.Monitoring) {
                val clientId = call.request.origin.remoteHost
                val currentTime = TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds

                val isAllowed = rateLimiter.isRequestAllowed(clientId, currentTime)
                if (!isAllowed) {
                    call.respond(ApiResponse<Unit>().setErrorMessage(429,"Too many requests. Please try later"))
                    finish()
                }
            }
            return rateLimiter
        }
    }

    suspend fun isRequestAllowed(clientId: String, currentTime: Long): Boolean {
        mutex.withLock(){
            val timeFrame =  60_000L
            val requestTimeStamps = requestCounts.getOrPut(clientId) { mutableListOf() }
            requestTimeStamps.removeAll { currentTime - it > timeFrame }

            if (requestTimeStamps.size >= config.requestsPerMinute) {
                return false
            }

            requestTimeStamps.add(currentTime)
            return true
        }
    }

}