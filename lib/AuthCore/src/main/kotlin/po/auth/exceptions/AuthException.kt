package po.auth.exceptions

import po.misc.exceptions.ExceptionPayload
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.ManageableException

enum class AuthErrorCode(val value: Int) {
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
        fun getByValue(value: Int): AuthErrorCode {
            entries.firstOrNull { it.value == value }?.let {
                return it
            }
            return UNDEFINED
        }
    }
}

class AuthException(
    override var message: String,
    override val code: AuthErrorCode,
    original : Throwable? = null
) : ManagedException(message, code, original){

    override var handler : HandlerType = HandlerType.CancelAll

    constructor(authPayload: ExceptionPayload) : this(authPayload.message, authPayload.code as AuthErrorCode, authPayload.cause){
        payload = authPayload
    }

}
