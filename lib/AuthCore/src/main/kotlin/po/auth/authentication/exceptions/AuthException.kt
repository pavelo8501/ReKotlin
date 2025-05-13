package po.auth.authentication.exceptions

import po.lognotify.exceptions.LoggerException
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException
import po.misc.types.safeCast
import kotlin.reflect.full.companionObjectInstance

enum class ErrorCodes(val value: Int) {
    UNDEFINED(0),
    INVALID_KEY_FORMAT(1002),
    INVALID_TOKEN(1003),
    TOKEN_EXPIRED(104),
    TOKEN_GENERATION_FAILED(1006),
    TOKEN_ISSUER_MISMATCH(1007),
    TOKEN_AUDIENCE_MISMATCH(1008),
    TOKEN_SIGNATURE_MISMATCH(1009),
    TOKEN_REVOKED(1010),
    TOKEN_INVALID_CLAIM(1011),
    TOKEN_INVALID_USER(1012),
    TOKEN_INVALID_ROLE(1013),
    TOKEN_INVALID_PERMISSION(1014),
    TOKEN_INVALID_AUDIENCE(1015),
    TOKEN_INVALID_ISSUER(1016),
    UNINITIALIZED(1018),

    INVALID_CREDENTIALS(4001),
    PASSWORD_MISMATCH(4002),

    INTERNAL_ERROR(5000),
    CONFIGURATION_MISSING(5002),
    SESSION_NOT_FOUND(5001),
    SESSION_PARAM_FAILURE(5002);


    companion object {

        fun getByValue(value: Int): ErrorCodes {
            ErrorCodes.entries.firstOrNull { it.value == value }?.let {
                return it
            }
            return UNDEFINED
        }
    }
}

class AuthException(
    override var message: String,
    val code: ErrorCodes,
) : ManagedException(message){


    override var handler : HandlerType = HandlerType.CANCEL_ALL


    companion object : SelfThrownException.Builder<AuthException> {
        override fun build(message: String, optionalCode: Int?): AuthException {
            val exCode = ErrorCodes.getByValue(optionalCode ?: 0)
            return AuthException(message, exCode)
        }
    }

//    companion object {
//        inline fun <reified E : ManagedException> build(message: String, optionalCode: Int?): E {
//            return E::class.companionObjectInstance?.safeCast<Builder<E>>()
//                ?.build(message, optionalCode)
//                ?: throw IllegalStateException("Companion object must implement Builder<E>")
//        }
//
//        interface Builder<E> {
//            fun build(message: String, optionalCode: Int?): E
//        }
//    }


}