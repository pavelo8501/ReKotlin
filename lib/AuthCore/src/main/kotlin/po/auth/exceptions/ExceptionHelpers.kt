package po.auth.exceptions

fun authException(message: String, code: AuthErrorCode, original: Throwable? = null): AuthException{
 return   AuthException(message, code, original)
}