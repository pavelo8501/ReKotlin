package po.api.rest_service.exceptions

enum class DataErrorCodes(val code: Int) {
    UNKNOWN_ERROR(0),
    DATA_SERIALIZATION_ERROR(1),
    DATA_DESERIALIZATION_ERROR(2),
    REQUEST_DATA_MISMATCH(3);

    companion object {
        fun fromValue(code: Int): DataErrorCodes? {
            DataErrorCodes.entries.firstOrNull { it.code == code }?.let {
                return it
            }
            return UNKNOWN_ERROR
        }
    }
}


class DataException(
    val errorCode: DataErrorCodes = DataErrorCodes.UNKNOWN_ERROR,
    override var message: String = "Data processing failed"
) : Throwable() {
}

