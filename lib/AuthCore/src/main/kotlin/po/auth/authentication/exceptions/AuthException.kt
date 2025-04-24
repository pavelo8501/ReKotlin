package po.auth.authentication.exceptions

import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException

enum class ErrorCodes(val code: Int) {

    ABNORMAL_STATE(0),
    INVALID_CREDENTIALS(1001),
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
    CONFIGURATION_MISSING(1017),

    SESSION_NOT_FOUND(2001),

    AUTH_PRINCIPAL_NOT_FOUND(3000),
    AUTH_PASSWORD_MISMATCH(3001);


    companion object {
        fun fromValue(code: Int): ErrorCodes {
            entries.firstOrNull { it.code == code }?.let {
                return it
            }
            return ABNORMAL_STATE
        }
    }
}

class AuthException(
    override var message: String,
    val code: ErrorCodes,
    override var handler : HandlerType = HandlerType.CANCEL_ALL
) : ManagedException(message, handler){

    override val builderFn: (String, HandlerType) -> AuthException = {msg, handler->
        AuthException(msg, code, handler)
    }


}