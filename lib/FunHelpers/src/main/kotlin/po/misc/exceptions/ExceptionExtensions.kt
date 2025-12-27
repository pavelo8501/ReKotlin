package po.misc.exceptions

import po.misc.context.tracable.TraceableContext
import po.misc.data.helpers.orDefault


fun Throwable.toManaged(context: TraceableContext): ManagedException{
    val managed = ManagedException(context, message = throwableToText(), code = null, cause = this)
    return managed
}

fun Throwable.throwableToText(): String{
    return buildString {
        appendLine(this@throwableToText.javaClass.simpleName)
        appendLine("Message: " +  message.orDefault("-") )
    }
}
