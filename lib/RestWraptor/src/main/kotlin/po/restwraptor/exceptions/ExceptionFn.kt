package po.restwraptor.exceptions

import po.misc.exceptions.HandlerType

fun throwConfiguration(message: String, code: ExceptionCodes, handler : HandlerType = HandlerType.CANCEL_ALL): Nothing{
   throw  ConfigurationException(message, code).setHandler(handler)

}