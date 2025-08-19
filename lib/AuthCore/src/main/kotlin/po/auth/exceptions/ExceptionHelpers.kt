package po.auth.exceptions

import po.misc.exceptions.ManagedCallSitePayload


fun authException(message: String, code: AuthErrorCode, original: Throwable? = null): AuthException{
 return   AuthException(message, code, original)
}

fun authException(payload: ManagedCallSitePayload): AuthException{
 return   AuthException(payload)
}
