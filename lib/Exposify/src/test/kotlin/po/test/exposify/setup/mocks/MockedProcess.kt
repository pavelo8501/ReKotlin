package po.test.exposify.setup.mocks

import po.auth.sessions.models.AuthorizedSession
import po.lognotify.process.Process
import po.lognotify.process.createProcess


val mockedProcess: Process<AuthorizedSession> =  createProcess(mockedSession)

fun mockProcess(session: AuthorizedSession):Process<AuthorizedSession>{
  return  createProcess(session)
}
