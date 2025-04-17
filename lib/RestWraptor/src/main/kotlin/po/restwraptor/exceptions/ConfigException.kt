package po.restwraptor.exceptions

import po.lognotify.exceptions.CancellationException
import po.lognotify.exceptions.ExceptionBase
import po.lognotify.exceptions.enums.HandlerType


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
    var type : HandlerType,
    override var message: String,
    val code: Int = 0
) :  CancellationException (message, type, code)