package po.restwraptor.models.configuration


class ApiConfig(
    var enableRateLimiting: Boolean  = true,
    var enableDefaultSecurity: Boolean = true,
    val enableDefaultCors: Boolean = true,
    val enableDefaultContentNegotiation: Boolean = true,
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
            adjustConfig()
        }

    var privateKeyString: String? = null
    var publicKeyString: String? = null
    var useWellKnownHost: Boolean = false
    var wellKnownPath: String? = null
    fun setAuthKeys(publicKey: String, privateKey: String) {
        this.publicKeyString = publicKey
        this.privateKeyString = privateKey
        this.wellKnownPath = null
        adjustConfig()
    }

    fun setWellKnown(path: String) {
        this.wellKnownPath = path
        this.publicKeyString = null
        this.privateKeyString = null
        adjustConfig()
    }

    private fun adjustConfig() {
        if(wellKnownPath != null) {
            enableDefaultSecurity = true
            useWellKnownHost = true
        }

        if(privateKeyString != null && publicKeyString != null) {
            enableDefaultSecurity = true
            useWellKnownHost = false
        }
        if(this._rateLimiterConfig != null) {
            enableRateLimiting = true
        }
    }
}