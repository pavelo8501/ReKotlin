package po.restwraptor.models.security

data class AuthenticatedModel(
    val token: String,
    val success: Boolean,
    val id: Long = 0
)
