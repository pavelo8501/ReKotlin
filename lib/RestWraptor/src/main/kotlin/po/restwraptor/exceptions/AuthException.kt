package po.restwraptor.exceptions

enum class AuthErrorCodes(val code: Int) {

    UNKNOWN_ERROR(0),
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
        fun fromValue(code: Int): AuthErrorCodes? {
            AuthErrorCodes.entries.firstOrNull { it.code == code }?.let {
                return it
            }
            return UNKNOWN_ERROR
        }
    }
}

class AuthException(
    val errorCode: AuthErrorCodes = AuthErrorCodes.UNKNOWN_ERROR,
    override var message: String = "Authentication failed"
) : Throwable() {
}