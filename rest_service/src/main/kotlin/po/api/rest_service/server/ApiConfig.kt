package po.api.rest_service.server

import po.api.rest_service.plugins.Jwt
import po.api.rest_service.plugins.RateLimiterConfig

data class ApiConfig(
    var enableRateLimiting: Boolean  = true,
    var enableDefaultSecurity: Boolean = true,
    var enableDefaultJwt: Boolean = true,
    val enableDefaultCors: Boolean = true,
    val enableDefaultContentNegotiation: Boolean = true,
) {

    var baseApiRoute = "/api"

    var useWellKnownHost: Boolean = false
    private var _wellKnownPath: String? = null
    var wellKnownPath: String?
        get() = _wellKnownPath
        set(value) {
            _wellKnownPath = value
            adjustConfig()
        }

    private var _rateLimiterConfig: RateLimiterConfig? = null
    var rateLimiterConfig: RateLimiterConfig = RateLimiterConfig()
        get() {
            return if(_rateLimiterConfig == null) {
                field
            }else{
                _rateLimiterConfig!!
            }
        }
        set(value) {
            field = value
            adjustConfig()
        }

    private fun adjustConfig() {
        if(wellKnownPath != null) {
            enableDefaultSecurity = true
            useWellKnownHost = true
        }
        if(this._rateLimiterConfig != null) {
            enableRateLimiting = true
        }
    }

}