package po.exposify.dto.interfaces

import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession
import po.misc.exceptions.CoroutineInfo
import po.misc.exceptions.getCoroutineInfo
import kotlin.coroutines.coroutineContext


interface RunnableContext: SessionIdentified{
    val session: AuthorizedSession?
    val coroutineInfo: CoroutineInfo

    override val sessionID: String
        get() = session?.sessionID?:"N/A"

    override val remoteAddress: String
        get() = session?.remoteAddress?:"N/A"

    companion object{

        data class RunInfo (
            override val session: AuthorizedSession?,
            override val coroutineInfo : CoroutineInfo
        ) : RunnableContext{
            override val remoteAddress: String
                get() = session?.remoteAddress?:"N/A"
            override val sessionID: String
                get() = session?.sessionID?:"N/A"
        }

        fun createRunInfo(session: AuthorizedSession?, coroutineInfo : CoroutineInfo):RunnableContext{
            return RunInfo(session, coroutineInfo)
        }
    }
}