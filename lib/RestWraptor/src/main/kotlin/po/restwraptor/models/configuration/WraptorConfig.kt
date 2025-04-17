package po.restwraptor.models.configuration

import po.restwraptor.enums.EnvironmentType

class WraptorConfig(
    var enviromnent : EnvironmentType = EnvironmentType.BUILD
) {
    var apiConfig = ApiConfig()
        private set

    var authConfig = AuthenticationConfig()
        private set

    internal fun updateApiConfig(config : ApiConfig){
        apiConfig = config
    }
    internal fun updateAuthConfig(config : AuthenticationConfig){
        authConfig = config
    }

}