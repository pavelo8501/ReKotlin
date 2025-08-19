package po.exposify.exceptions

import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.misc.context.CTX
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedCallSitePayload

import po.misc.exceptions.ManagedException


class InitException(
    override val msg: String,
    override val code: ExceptionCode?,
    val callingContext: CTX?,
    override val cause: Throwable? = null,
): ManagedException(msg, code, cause) {

    override var handler: HandlerType = HandlerType.CancelAll
    override val context: CTX? get() = callingContext

    constructor(
        payload: ManagedCallSitePayload
    ): this(
        msg = payload.message,
        code = payload.code as ExceptionCode,
        cause = payload.cause,
        callingContext = payload.context,
    ){
        initFromPayload(payload)
    }

}

class OperationsException(
    override val msg: String,
    override val code: ExceptionCode?,
    val callingContext: CTX?,
    override val cause: Throwable? = null,
) : ManagedException (msg, code) {


    override var handler: HandlerType = HandlerType.CancelAll
    override val context: CTX? get() = callingContext

    constructor(
        payload: ManagedCallSitePayload
    ): this(
        msg = payload.message,
        code = payload.code as ExceptionCode,
        cause = payload.cause,
        callingContext = payload.context
    ){
        initFromPayload(payload)
    }
}

