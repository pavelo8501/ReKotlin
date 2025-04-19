package po.auth.sessions.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import po.auth.authentication.interfaces.AuthenticationPrincipal



@Serializable
open class AuthorizedPrincipal(
    override val userId: Long = 0L,
    override val login: String = "no_name",
    override val email: String = "nomail@undeliverable.void",
    override val userGroupId: Long = 0L,
    override val roles: Set<String> = emptySet(),
) : AuthenticationPrincipal {

    fun copyReinit(src: AuthenticationPrincipal):AuthorizedPrincipal{
       return AuthorizedPrincipal(src.userId, src.login, src.email, src.userGroupId, src.roles)
    }

    override fun asJson(): String {
        return Json.encodeToString(this)
    }

}
