package po.restwraptor.models.configuration

import io.ktor.server.application.Application
import po.lognotify.shared.enums.HandleType
import po.restwraptor.enums.EnvironmentType
import po.restwraptor.exceptions.ConfigurationErrorCodes
import po.restwraptor.exceptions.ConfigurationException

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