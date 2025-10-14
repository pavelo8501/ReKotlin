package po.auth.exceptions

import po.misc.exceptions.ThrowableCallSitePayload


fun authException(message: String, code: AuthErrorCode, original: Throwable? = null): AuthException{
 return   AuthException(message, code, original)
}

fun authException(payload: ThrowableCallSitePayload): AuthException{
 return   AuthException(payload)
}
