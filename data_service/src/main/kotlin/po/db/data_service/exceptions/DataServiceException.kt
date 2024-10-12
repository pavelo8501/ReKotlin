package po.db.data_service.exceptions



enum class ErrorCodes (val errorCode:Int){
    UNDEFINED(0),
    NOT_INITIALIZED(1001),
    ALREADY_EXISTS(1002),
    INVALID_DATA(1003);

    companion object {
        fun fromValue(errorCode: Int): ErrorCodes? {
            ErrorCodes.entries.firstOrNull { it.errorCode == errorCode }?.let {
                return it
            }
            return UNDEFINED
        }
    }

}


class DataServiceException(message : String, val errorCode:ErrorCodes) : Throwable(message)  {





}