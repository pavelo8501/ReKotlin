package po.exposify.exceptions


import po.misc.exceptions.ExceptionPayload
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedCallSitePayload

import po.misc.exceptions.ManagedException


class InitException(
    payload: ManagedCallSitePayload
): ManagedException(payload) {
    override var handler: HandlerType = HandlerType.SkipSelf
}

class OperationsException(
    payload: ManagedCallSitePayload
) : ManagedException (payload) {
    override var handler : HandlerType = HandlerType.SkipSelf
}

