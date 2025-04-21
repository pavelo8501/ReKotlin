package po.auth.sessions.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import po.auth.authentication.interfaces.AuthenticationPrincipal


@Serializable
open class AuthorizedPrincipal(
    override val id: Long = 0L,
    override val login: String = "no_name",
    override val email: String = "nomail@undeliverable.void",
    override val userGroupId: Long = 0L,
) : AuthenticationPrincipal {

    fun copyReinit(src: AuthenticationPrincipal):AuthorizedPrincipal{
       return AuthorizedPrincipal(src.id, src.login, src.email, src.userGroupId)
    }

    override fun asJson(): String {
        return Json.encodeToString(this)
    }

}
