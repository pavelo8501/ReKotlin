package po.restwraptor.models.configuration

data class RateLimiterConfig(
    var requestsPerMinute: Int = 60,
    var suspendInSeconds: Int = 60
)

