package po.exposify.dto.interfaces

import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.models.SequenceRunInfo
import po.misc.coroutines.CoroutineInfo
import kotlin.coroutines.coroutineContext


interface RunnableContext: SessionIdentified{
    val session: AuthorizedSession
    val coroutineInfo: CoroutineInfo

    override val sessionID: String
        get() = session.sessionID

    override val remoteAddress: String
        get() = session.remoteAddress

    companion object{

       suspend fun runInfo(session: AuthorizedSession):SequenceRunInfo{
            val coroutineInfo = CoroutineInfo.createInfo(coroutineContext)
            return SequenceRunInfo(session, coroutineInfo)
        }
    }
}