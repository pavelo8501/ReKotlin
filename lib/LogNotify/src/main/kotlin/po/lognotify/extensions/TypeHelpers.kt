package po.lognotify.extensions

import po.lognotify.exceptions.LoggerException
import po.misc.context.Identifiable
import po.misc.types.getOrThrow


internal inline fun <reified T : Any> T?.getOrLoggerEx(
    ctx: Identifiable? = null
): T {
    return this.getOrThrow<T>(){
        LoggerException(it)
    }
}