package po.auth.authentication.jwt.models

data class JwtToken(
   val sessionId: String,
   val token : String,
)