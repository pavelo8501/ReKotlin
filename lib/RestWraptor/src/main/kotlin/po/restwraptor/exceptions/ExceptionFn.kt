package po.restwraptor.exceptions

import po.misc.context.CTX
import po.misc.exceptions.HandlerType
import po.misc.context.Identifiable


fun CTX.throwConfiguration(message: String, code: ExceptionCodes, handler : HandlerType = HandlerType.CancelAll): Nothing{

   val exception =  ConfigurationException(message, code, null)
   exception.setHandler(handler, this)
   throw  exception
}

