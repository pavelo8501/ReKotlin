package po.restwraptor.exceptions

import po.misc.context.TraceableContext


internal fun TraceableContext.configException( message: String, code: ExceptionCodes, original: Throwable? = null): ConfigurationException{
  return  ConfigurationException(this, message, code, original)
}