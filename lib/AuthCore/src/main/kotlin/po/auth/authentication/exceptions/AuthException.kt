package po.auth.authentication.exceptions

enum class ErrorCodes(val code: Int) {

    UNKNOWN(0),
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
    CONFIGURATION_MISSING(1017);

    companion object {
        fun fromValue(code: Int): ErrorCodes {
            entries.firstOrNull { it.code == code }?.let {
                return it
            }
            return UNKNOWN
        }
    }
}

class AuthException(
    override var message: String,
    val errorCode: ErrorCodes = ErrorCodes.UNKNOWN,
) : Throwable(message)