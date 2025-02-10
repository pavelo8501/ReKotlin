package po.lognotify.eventhandler.convenience

import po.lognotify.eventhandler.EventHandlerBase
import po.lognotify.eventhandler.exceptions.ProcessableException

fun <T: EventHandlerBase>  T.throwSkip(message: String):ProcessableException{
    return this.throwSkipException(message)
}
fun <T: EventHandlerBase>  T.throwPropagate(message: String, cancelFn: (()->Unit)?):ProcessableException{
    return this.throwPropagate(message,cancelFn)
}

fun <T: EventHandlerBase>  T.throwCancel(message: String,  cancelFn: (() -> Unit)? ):ProcessableException {
    return this.throwCancelException(message, cancelFn)
}