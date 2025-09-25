package po.restwraptor.exceptions


import po.misc.exceptions.ManagedException
import po.misc.exceptions.TraceableContext


class ConfigurationException(
     context: TraceableContext,
     message: String,
     code: ExceptionCodes = ExceptionCodes.UNKNOWN,
     cause: Throwable? = null
) :  ManagedException(context, message, code, cause){


}