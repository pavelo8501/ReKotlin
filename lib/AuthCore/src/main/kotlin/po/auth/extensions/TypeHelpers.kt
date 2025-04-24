package po.auth.extensions

import po.auth.authentication.exceptions.AuthException
import po.auth.authentication.exceptions.ErrorCodes
import po.misc.exceptions.castOrException
import po.misc.exceptions.getOrException


internal fun <T> T?.getOrThrow(message: String, code: ErrorCodes):T{
    return this.getOrException(){
        throw AuthException(message, code)
    }
}

inline fun <reified T: Any> Any.castOrThrow(message: String, code: ErrorCodes): T {
    return this.castOrException {
        val exception = AuthException(message, code)
        exception.addMessage("$message. Cast to ${T::class.simpleName } failed")
    }
}


inline fun <T>  T.ifTestPass(predicate: (T)-> Boolean):T?{

    return if(predicate(this)){
        this
    }else{
        null
    }
}
