package po.lognotify.test.testmodels

import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.shared.enums.HandleType


class TestException(message: String, type: HandleType) :   ProcessableException(message, type)

class TestSkipException : ProcessableException("TestSkipException", HandleType.SKIP_SELF)