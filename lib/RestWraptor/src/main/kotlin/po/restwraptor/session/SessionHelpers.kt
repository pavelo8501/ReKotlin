package po.restwraptor.session

import io.ktor.server.application.ApplicationCall
import io.ktor.util.AttributeKey
import po.auth.sessions.models.AuthorizedSession




fun  ApplicationCall.sessionFromAttributes(
): AuthorizedSession? {
    val key =  AttributeKey<AuthorizedSession>("AuthSession")
    return attributes.getOrNull(key)
}

fun  ApplicationCall.toAttributes(
    session: AuthorizedSession
): AuthorizedSession{
    val key =  AttributeKey<AuthorizedSession>("AuthSession")
   attributes.put(key, session)
   return session
}



fun  ApplicationCall.currentSessionOrNew():AuthorizedSession? {
    val authorizedSessionKey = AttributeKey<AuthorizedSession>("AuthSession")
    return attributes.takeOrNull(authorizedSessionKey)
}