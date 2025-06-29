package po.restwraptor.exceptions

internal fun configException(message: String, code: ExceptionCodes, original: Throwable? = null): ConfigurationException{
  return  ConfigurationException(message, code, original)
}