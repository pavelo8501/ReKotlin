package po.restwraptor.exceptions

import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.shared.enums.HandleType

enum class ConfigurationErrorCodes(val code: Int) {
    UNKNOWN_ERROR(0),
    UNABLE_TO_CALLBACK(3001),
    REQUESTING_UNDEFINED_PLUGIN(3002),
    PLUGIN_SETUP_FAILURE(3003);
    companion object {
        fun fromValue(code: Int): ConfigurationErrorCodes? {
            ConfigurationErrorCodes.entries.firstOrNull { it.code == code }?.let {
                return it
            }
            return UNKNOWN_ERROR
        }
    }
}

class ConfigurationException(
    override var message: String,
    var type : HandleType,
    var errorCode: ConfigurationErrorCodes = ConfigurationErrorCodes.UNKNOWN_ERROR,
) : ProcessableException(message,type)