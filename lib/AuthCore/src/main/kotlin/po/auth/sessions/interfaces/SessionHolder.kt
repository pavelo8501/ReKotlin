package po.auth.sessions.interfaces


import po.auth.AuthSessionManager
import po.auth.sessions.models.ScopedSession
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.exceptions.ManagedException
import po.misc.types.safeCast


interface SessionHolder<T: CTX> : CTX{
    val session: ScopedSession<T>
}

//inline fun <reified T: CTX>  SessionHolder<T>.scopedSession():ScopedSession<T>{
//  val session =  ScopedSession(this.identity)
//   AuthSessionManager.registerScopedSession(session)
//    return session as ScopedSession<T>
//}
//

inline fun <reified T: CTX> T.scopedSession(): ScopedSession<T> {

    @Suppress("SENSELESS_COMPARISON")
    if (identity != null) {
        identity.safeCast<CTXIdentity<T>>()?.let {
            return AuthSessionManager.registerScopedSession(ScopedSession(it))
        }
    }
    throw ManagedException("ScopedSession creation failure. CTXIdentity of receiver class is not yet constructed.")
}

class TestHolder : CTX,  SessionHolder<TestHolder> {
    override val identity = asIdentity()
    override val session: ScopedSession<TestHolder> = scopedSession()
}