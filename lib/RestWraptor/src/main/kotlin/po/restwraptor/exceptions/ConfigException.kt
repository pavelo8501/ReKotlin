package po.restwraptor.exceptions


import po.misc.exceptions.ManagedException


class ConfigurationException(
    override var message: String,
    code: ExceptionCodes = ExceptionCodes.UNKNOWN,
     original: Throwable? = null
) :  ManagedException(message, code, original){


}