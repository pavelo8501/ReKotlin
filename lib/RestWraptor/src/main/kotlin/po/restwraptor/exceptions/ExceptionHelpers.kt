package po.restwraptor.exceptions

import po.misc.context.tracable.TraceableContext


internal fun TraceableContext.configException( message: String, code: ExceptionCodes, original: Throwable? = null): ConfigurationException{
  return  ConfigurationException(this, message, code, original)
}