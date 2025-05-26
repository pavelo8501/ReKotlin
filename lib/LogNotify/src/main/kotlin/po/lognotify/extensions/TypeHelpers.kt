package po.lognotify.extensions

import po.lognotify.exceptions.LoggerException
import po.misc.types.getOrThrow


internal inline fun <reified T : Any> T?.getOrLoggerEx(
    message: String? = null,
): T {
    return this.getOrThrow<T, LoggerException>(message)
}