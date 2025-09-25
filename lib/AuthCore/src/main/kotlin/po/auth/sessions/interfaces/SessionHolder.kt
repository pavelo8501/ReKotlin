package po.auth.sessions.interfaces

import po.auth.sessions.models.ScopedSession
import po.misc.context.CTX


interface SessionHolder : CTX {
    val session: ScopedSession
}


fun  CTX.scopedSession(): ScopedSession {
   return ScopedSession(this)
}
