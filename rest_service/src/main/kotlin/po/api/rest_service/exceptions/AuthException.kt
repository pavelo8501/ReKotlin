package po.api.rest_service.exceptions


enum class AuthErrorCodes(val code: Int) {

    UNKNOWN_ERROR(0),
    INVALID_CREDENTIALS(1),
    INVALID_KEY_FORMAT(2),
    INVALID_TOKEN(3),
    TOKEN_EXPIRED(4),
    TOKEN_ISSUER_MISMATCH(5),
    TOKEN_AUDIENCE_MISMATCH(6),
    TOKEN_SIGNATURE_MISMATCH(7),
    TOKEN_REVOKED(8),
    TOKEN_INVALID_CLAIM(9),
    TOKEN_INVALID_USER(10),
    TOKEN_INVALID_ROLE(11),
    TOKEN_INVALID_PERMISSION(12),
    TOKEN_INVALID_AUDIENCE(13),
    TOKEN_INVALID_ISSUER(14),
    CONFIGURATION_MISSING(15);

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