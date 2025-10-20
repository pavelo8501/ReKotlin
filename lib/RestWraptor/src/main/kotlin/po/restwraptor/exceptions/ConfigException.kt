package po.restwraptor.exceptions


import po.misc.exceptions.ManagedException
import po.misc.context.tracable.TraceableContext


class ConfigurationException(
     context: TraceableContext,
     message: String,
     code: ExceptionCodes = ExceptionCodes.UNKNOWN,
     cause: Throwable? = null
) :  ManagedException(context, message, code, cause){


}