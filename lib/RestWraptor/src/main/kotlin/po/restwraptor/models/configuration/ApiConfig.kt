package po.restwraptor.models.configuration

import po.restwraptor.interfaces.SecuredUserInterface
import po.restwraptor.models.request.LoginRequest
import java.io.File


class ApiConfig(
    var rateLimiting: Boolean  = true,
    val cors: Boolean = true,
    val contentNegotiation: Boolean = true,
    var defaultRouts: Boolean  = false,
) {

    var baseApiRoute = "/api"

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
        }

}