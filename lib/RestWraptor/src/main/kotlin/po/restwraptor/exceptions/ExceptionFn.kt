package po.restwraptor.exceptions

import po.lognotify.exceptions.enums.HandlerType

fun throwConfiguration(message: String, code: ExceptionCodes, handler : HandlerType = HandlerType.CANCEL_ALL): Nothing{
   throw ConfigurationException(message, code, handler)
}