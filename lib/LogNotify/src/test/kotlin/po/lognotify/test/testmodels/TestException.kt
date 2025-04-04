package po.lognotify.test.testmodels

import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.shared.enums.HandleType


class TestException(message: String, customParam: Int) :   ProcessableException(HandleType.UNMANAGEABLE,message)
class TestCancelException(message: String) :
    ProcessableException(HandleType.UNMANAGEABLE,message)



class TestSkipException : ProcessableException(HandleType.SKIP_SELF, "TestSkipException")