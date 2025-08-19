package po.restwraptor.models.configuration

import po.restwraptor.enums.EnvironmentType


data class ApiConfig(
    var environment : EnvironmentType = EnvironmentType.BUILD,
    var rateLimiting: Boolean  = true,
    var cors: Boolean = true,
    var contentNegotiation: Boolean = true,
    var systemRouts : Boolean = true,
) {
    private var _rateLimiterConfig: RateLimiterConfig? = null
    var rateLimiterConfig: RateLimiterConfig = RateLimiterConfig()
        get() {
            return if(_rateLimiterConfig == null) {
                field
            }else{ _rateLimiterConfig!! }
        }
        set(value) { field = value }

}