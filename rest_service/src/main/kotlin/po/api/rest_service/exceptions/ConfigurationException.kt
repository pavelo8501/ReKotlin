package po.api.rest_service.exceptions



enum class ConfigurationErrorCodes(val code: Int) {

    UNKNOWN_ERROR(0),
    UNABLE_TO_CALLBACK(3001);

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
    val errorCode: ConfigurationErrorCodes = ConfigurationErrorCodes.UNKNOWN_ERROR,
    override var message: String = "UNKNOWN_ERROR"
) : Throwable() {
}