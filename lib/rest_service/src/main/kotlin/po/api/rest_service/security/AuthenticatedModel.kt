package po.api.rest_service.security

class AuthenticatedModel(
    val token: String,
    val success: Boolean,
    val id: Long = 0
)