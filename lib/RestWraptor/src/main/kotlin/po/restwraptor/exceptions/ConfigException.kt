package po.restwraptor.exceptions

import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.shared.enums.HandleType

enum class ConfigurationErrorCodes(val value: Int) {
    UNKNOWN(0),
    UNABLE_TO_CALLBACK(3001),
    REQUESTING_UNDEFINED_PLUGIN(3002),
    PLUGIN_SETUP_FAILURE(3003),
    SERVICE_SETUP_FAILURE(3004),
    API_CONFIG_FATAL_ERROR(3005);
    companion object {
        fun fromValue(code: Int): ConfigurationErrorCodes? {
            ConfigurationErrorCodes.entries.firstOrNull { it.value == code }?.let {
                return it
            }
            return UNKNOWN
        }
    }
}

class ConfigurationException(
    var type : HandleType,
    override var message: String,
    val code: Int = 0
) : ProcessableException(type, message, code)