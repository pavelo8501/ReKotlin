package po.restwraptor.exceptions


enum class ExceptionCodes(val value: Int) {
    UNKNOWN(0),

    //Call processing codes
    DATA_SERIALIZATION_ERROR(2001),
    DATA_DESERIALIZATION_ERROR(2002),
    REQUEST_DATA_MISMATCH(2003),
    VALUE_IS_NULL(2004),

    //Configuration codes
    GENERAL_CONFIG_FAILURE(3001),
    KEY_REGISTRATION(3002),
    REQUESTING_UNDEFINED_PLUGIN(3003),
    PLUGIN_SETUP_FAILURE(3004),
    SERVICE_SETUP_FAILURE(3005),
    API_CONFIG_FATAL_ERROR(3006),
    LOGNOTIFY_ERROR(3007),

    GENERAL_AUTH_CONFIG_FAILURE(4001),
    AUTH_SERVICE_EXCEPTION(4002);


    companion object {
        init {
            val duplicates = entries.groupBy { it.value }
                .filter { it.value.size > 1 }
                .keys
            require(duplicates.isEmpty()) {
                "Duplicate enum ExceptionCodes values found: $duplicates"
            }
        }

        fun getByValue(value: Int): ExceptionCodes {
            ExceptionCodes.entries.firstOrNull { it.value == value }?.let {
                return it
            }
            return UNKNOWN
        }
    }

}
