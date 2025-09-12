package po.test.exposify.setup.mocks

import po.auth.sessions.models.SessionBase
import po.lognotify.process.Process
import po.lognotify.process.createProcess


val mockedProcess: Process<SessionBase> =  createProcess(mockedSession)

fun mockProcess(session: SessionBase):Process<SessionBase>{
  return  createProcess(session)
}
