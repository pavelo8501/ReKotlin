package po.auth.authentication.extensions

import po.auth.authentication.exceptions.AuthException
import po.auth.authentication.exceptions.ErrorCodes
import po.lognotify.exceptions.ManagedException
import po.lognotify.extensions.castOrException
import po.lognotify.extensions.getOrException

fun <T> T?.getOrThrow(message: String, code: ErrorCodes):T{
   return this.getOrException(AuthException(message, code))
}

inline fun <reified T: Any> Any.castOrThrow(message: String, code: ErrorCodes): T {
   return this.castOrException<T, AuthException>(AuthException(message, code))
}