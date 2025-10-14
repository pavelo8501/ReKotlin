package po.exposify.dto.interfaces

import kotlinx.coroutines.currentCoroutineContext
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.models.SequenceRunInfo
import po.misc.coroutines.CoroutineInfo


interface RunnableContext: SessionIdentified {
    val session: AuthorizedSession
    val coroutineInfo: CoroutineInfo

    override val ip: String get() = session.ip
    override val userAgent: String get() = session.userAgent

    companion object {

        suspend fun runInfo(session: AuthorizedSession): SequenceRunInfo {
            val context = currentCoroutineContext()
            val coroutineInfo = CoroutineInfo.createInfo(context)
            return SequenceRunInfo(session, coroutineInfo)
        }
    }
}