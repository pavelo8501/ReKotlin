package po.auth.authentication.jwt.models

data class JwtToken(
   val token : String,
   val sessionId: String
)