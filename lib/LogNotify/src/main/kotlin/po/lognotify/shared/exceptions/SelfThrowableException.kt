package po.lognotify.shared.exceptions

import po.lognotify.eventhandler.exceptions.ProcessableException


interface SelfThrowableException{
    fun <T : ProcessableException> throwThis(ex: T, handler: ((T) -> Unit)? = null) {
        if(handler!=null){
            handler(ex)
        }
        throw ex
    }
}