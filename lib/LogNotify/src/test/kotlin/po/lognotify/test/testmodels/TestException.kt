package po.lognotify.test.testmodels

import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.shared.enums.HandleType


class TestException(message: String, customParam: Int) :   ProcessableException(message, HandleType.UNMANAGEABLE)
class TestCancelException(message: String) :
    ProcessableException(message, HandleType.UNMANAGEABLE)



class TestSkipException : ProcessableException("TestSkipException", HandleType.SKIP_SELF)