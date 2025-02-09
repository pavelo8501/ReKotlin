package po.exposify.exceptions

import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.shared.enums.HandleType

enum class  ExceptionCodes (val errorCode:Int) {
    UNDEFINED(0),
    INITIALIZATION_OUTSIDE_CONTEXT (1001),
    NOT_INITIALIZED (1002),
    ALREADY_EXISTS(1003),
    INVALID_DATA(1004),
    KEY_NOT_FOUND(1005),
    LAZY_NOT_INITIALIZED (1006),

    NO_EMPTY_CONSTRUCTOR(2001),
    REFLECTION_ERROR(2002),
    CONSTRUCTOR_MISSING(2003),

    DB_TABLE_CREATION_FAILURE(3001);

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
class OperationsException(message: String, code : ExceptionCodes) :
    ProcessableException(message, HandleType.PROPAGATE_TO_PARENT)