package po.restwraptor.models.configuration

import io.ktor.server.application.Application
import po.lognotify.shared.enums.HandleType
import po.restwraptor.exceptions.ConfigurationErrorCodes
import po.restwraptor.exceptions.ConfigurationException

class WraptorConfig(private val app: Application? = null) {

    var _application : Application? = app
    val application : Application
        get(){return  _application?: throw ConfigurationException("Application not defined", HandleType.CANCEL_ALL,
            ConfigurationErrorCodes.SERVICE_SETUP_FAILURE) }
    var baseApiRoute = "/api"

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

    fun setApplication(app: Application){
        _application = app
    }

}