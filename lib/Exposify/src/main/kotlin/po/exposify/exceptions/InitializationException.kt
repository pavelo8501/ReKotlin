package po.exposify.exceptions

import po.exposify.exceptions.enums.InitErrorCodes
import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.shared.enums.HandleType


class InitializationException(message: String, errCode : InitErrorCodes) :
    ProcessableException(HandleType.PROPAGATE_TO_PARENT, message, errCode.value)