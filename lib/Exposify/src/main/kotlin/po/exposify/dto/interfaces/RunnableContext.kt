package po.exposify.dto.interfaces

import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.interfaces.SessionIdentified
import po.auth.sessions.models.AuthorizedSession


interface RunnableContext: SessionIdentified{
    val method: String
    val sessionType: SessionType

    companion object{

        data class RunInfo (
            override val method: String,
            override val sessionType: SessionType,
            override val sessionID: String = "N/A",
            override val remoteAddress: String = "N/A"
        ) : RunnableContext{

        }

        fun createRunInfo(method: String, session: AuthorizedSession?):RunnableContext{
            if(session!=null){
                return RunInfo(method, session.sessionType, session.sessionID, session.remoteAddress)
            }else{
                return RunInfo(method, SessionType.ANONYMOUS)
            }

        }
    }
}