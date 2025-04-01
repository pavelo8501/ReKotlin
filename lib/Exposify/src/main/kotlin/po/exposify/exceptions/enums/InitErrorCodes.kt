package po.exposify.exceptions.enums

import po.exposify.exceptions.ExceptionCodes

enum class InitErrorCodes(val value:Int)  {

    UNDEFINED(0),
    KEY_PARAM_UNINITIALIZED(1001),
    ALREADY_EXISTS(1002),
    LAZY_NOT_INITIALIZED (1003),
    INVALID_DATA(1004),
    KEY_NOT_FOUND(1005),
    DB_TABLE_CREATION_FAILURE(2001),
    CAST_FAILURE(3001);


    companion object {
        fun fromValue(code: Int): InitErrorCodes {
            InitErrorCodes.entries.firstOrNull { it.value == code }?.let {
                return it
            }
            return UNDEFINED
        }

        fun toValue(errorCode: InitErrorCodes): Int {
          return  errorCode.value
        }
    }

}