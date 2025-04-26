package po.restwraptor.models.configuration

import po.restwraptor.enums.EnvironmentType
import po.restwraptor.interfaces.SecuredUserInterface
import po.restwraptor.models.request.LoginRequest
import java.io.File


data class ApiConfig(
    var environment : EnvironmentType = EnvironmentType.BUILD,
    var rateLimiting: Boolean  = true,
    var cors: Boolean = true,
    var contentNegotiation: Boolean = true,
    var systemRouts : Boolean = true,
    var baseApiRoute : String = "",
    var baseServiceRoute : String = ""
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