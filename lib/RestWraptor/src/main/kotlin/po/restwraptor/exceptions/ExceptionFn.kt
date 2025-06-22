package po.restwraptor.exceptions

import po.misc.exceptions.HandlerType
import po.misc.interfaces.IdentifiableContext

fun throwConfiguration(message: String, code: ExceptionCodes, ctx:  IdentifiableContext, handler : HandlerType = HandlerType.CANCEL_ALL): Nothing{
   val exception =  ConfigurationException(message, code, null)
   exception.setHandler(handler, ctx)
   throw  exception

}