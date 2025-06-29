package po.lognotify.extensions

import po.lognotify.exceptions.LoggerException
import po.misc.interfaces.IdentifiableContext
import po.misc.types.getOrThrow


internal inline fun <reified T : Any> T?.getOrLoggerEx(
    ctx: IdentifiableContext? = null
): T {
    return this.getOrThrow<T, LoggerException>(ctx){
        LoggerException(it?:"")
    }
}