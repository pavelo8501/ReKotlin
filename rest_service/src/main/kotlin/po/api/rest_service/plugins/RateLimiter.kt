package po.api.rest_service.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.call
import io.ktor.server.plugins.origin
import io.ktor.server.response.respondText
import io.ktor.util.AttributeKey
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class RateLimiterConfig {
    var requestsPerMinute: Int = 60
    var suspendInSeconds: Int = 60
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
                val currentTime = System.currentTimeMillis()

                val isAllowed = rateLimiter.isRequestAllowed(clientId, currentTime)
                if (!isAllowed) {
                    call.response.status(HttpStatusCode.TooManyRequests)
                    call.respondText("Too many requests. Please try later")
                    //call.respond(ApiResponse<Unit>().setErrorMessage(429,"Too many requests. Please try later"))
                    finish()
                }
            }
            return rateLimiter
        }
    }

    suspend fun isRequestAllowed(clientId: String, currentTime: Long): Boolean {

        mutex.withLock() {
            val timeFrame = config.suspendInSeconds * 1_000L
            val requestTimeStamps = requestCounts.getOrPut(clientId) { mutableListOf() }

            requestTimeStamps.removeAll { currentTime - it > timeFrame }

            if (requestTimeStamps.isEmpty()) {
                requestTimeStamps.add(currentTime)
                return true
            }
            if (requestTimeStamps.size >= config.requestsPerMinute) {
                if (currentTime - requestTimeStamps.first() > timeFrame) {
                    requestTimeStamps.clear()
                    requestTimeStamps.add(currentTime)
                    return true
                }
                return false
            }
            requestTimeStamps.add(currentTime)
            return true
        }
    }
}