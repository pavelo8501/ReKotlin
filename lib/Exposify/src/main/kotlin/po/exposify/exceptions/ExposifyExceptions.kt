package po.exposify.exceptions

import po.exposify.exceptions.enums.ExceptionCode
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ThrowableCallSitePayload

import po.misc.exceptions.ManagedException


class InitException(
    context: Any,
    message: String,
    override val code: ExceptionCode?,
    override val cause: Throwable? = null,
): ManagedException(context, message, code, cause) {

    override var handler: HandlerType = HandlerType.CancelAll
   // override val context: CTX? get() = callingContext

    constructor(
        payload: ThrowableCallSitePayload
    ): this(
        message = payload.message,
        code = payload.code as ExceptionCode,
        cause = payload.cause,
        context = payload.context,
    ){
        initFromPayload(payload)
    }

}

class OperationsException(
    context: Any,
    message: String,
    override val code: ExceptionCode?,
    override val cause: Throwable? = null,
) : ManagedException (context,  message, code) {


    override var handler: HandlerType = HandlerType.CancelAll


    constructor(
        payload: ThrowableCallSitePayload
    ): this(
        message = payload.message,
        code = payload.code as ExceptionCode,
        cause = payload.cause,
        context = payload.context
    ){
        initFromPayload(payload)
    }
}

