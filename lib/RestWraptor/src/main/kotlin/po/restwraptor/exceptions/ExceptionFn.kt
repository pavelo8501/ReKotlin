package po.restwraptor.exceptions

import po.misc.exceptions.HandlerType
import po.misc.context.Identifiable

fun throwConfiguration(message: String, code: ExceptionCodes, ctx:  Identifiable, handler : HandlerType = HandlerType.CancelAll): Nothing{
   val exception =  ConfigurationException(message, code, null)
   exception.setHandler(handler, ctx)
   throw  exception
}

