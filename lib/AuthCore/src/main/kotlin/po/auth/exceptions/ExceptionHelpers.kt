package po.auth.exceptions

import po.misc.exceptions.ExceptionPayload

fun authException(message: String, code: AuthErrorCode, original: Throwable? = null): AuthException{



 return   AuthException(message, code, null,  original)
}