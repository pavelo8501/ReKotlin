package po.exposify.dto.models

import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.interfaces.RunnableContext
import po.misc.coroutines.CoroutineInfo

data class SequenceRunInfo(
    override val session: AuthorizedSession,
    override val coroutineInfo: CoroutineInfo,
): RunnableContext
