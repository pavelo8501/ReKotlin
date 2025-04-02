package po.exposify.exceptions

import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.shared.enums.HandleType

enum class  ExceptionCodes (val value:Int) {
    UNDEFINED(0),
    INITIALIZATION_OUTSIDE_CONTEXT (1001),
    NOT_INITIALIZED (1002),
    ALREADY_EXISTS(1003),
    INVALID_DATA(1004),
    KEY_NOT_FOUND(1005),
    LAZY_NOT_INITIALIZED (1006),
    CAST_FAILURE(1007),

    NO_EMPTY_CONSTRUCTOR(2001),
    REFLECTION_ERROR(2002),
    CONSTRUCTOR_MISSING(2003),

    DB_TABLE_CREATION_FAILURE(3001),
    DB_CRUD_FAILURE(3002),

    REPOSITORY_RETHROWN(4000),
    REPOSITORY_NOT_INITIALIZED(4001),
    REPOSITORY_NOT_FOUND(4002),

    BINDING_PROPERTY_MISSING(5001),

    FACTORY_CREATE_FAILURE(6001);

    companion object {
        fun fromValue(errorCode: Int): ExceptionCodes {
            ExceptionCodes.entries.firstOrNull { it.value == errorCode }?.let {
                return it
            }
            return UNDEFINED
        }
    }
}

class OperationsException(message: String, errCode : ExceptionCodes) :
    ProcessableException(HandleType.PROPAGATE_TO_PARENT, message, errCode.value)