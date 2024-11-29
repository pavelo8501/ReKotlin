package po.db.data_service.exceptions

enum class  ExceptionCodes (val errorCode:Int) {
    UNDEFINED(0),
    INITIALIZATION_OUTSIDE_CONTEXT (1001),
    ALREADY_EXISTS(1002),
    INVALID_DATA(1003);

    companion object {
        fun fromValue(errorCode: Int): ExceptionCodes {
            ExceptionCodes.entries.firstOrNull { it.errorCode == errorCode }?.let {
                return it
            }
            return UNDEFINED
        }
    }
}


class TypeMismatchException(message: String) : RuntimeException(message)
class InitializationException(message: String, code : ExceptionCodes) : RuntimeException(message)