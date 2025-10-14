package po.exposify.exceptions

import po.exposify.exceptions.enums.ExceptionCode
import po.misc.context.CTX
import po.misc.exceptions.ThrowableCallSitePayload
import po.misc.exceptions.ManagedPayload



@PublishedApi
internal fun initException(payload: ThrowableCallSitePayload): InitException{
    payload.methodName = "initException"
    val exception = InitException(payload)
    return exception
}

@PublishedApi
internal fun initException(message: String, exceptionCode: ExceptionCode, context: CTX): InitException{
    val methodName: String = "initException"
    val payload = ManagedPayload(message, methodName, context)
    return InitException(payload.setCode(exceptionCode))
}

@PublishedApi
internal fun <T: CTX> T.initException(
    message: String,
    code: ExceptionCode
):InitException = initException(message, code, this)

internal fun initAbnormal(
    context: CTX,
): InitException = initException("Abnormal state", ExceptionCode.ABNORMAL_STATE, context)


internal fun badDTOSetup(
    context: CTX,
): InitException = initException("Bad DTO Setup", ExceptionCode.BAD_DTO_SETUP, context)



@PublishedApi
internal fun operationsException(
    payload: ThrowableCallSitePayload
): OperationsException{
    payload.methodName = "operationsException"
    return OperationsException(payload)
}

@PublishedApi
internal fun operationsException(
    message: String,
    exceptionCode: ExceptionCode,
    context: Any
): OperationsException{
    val methodName: String = "operationsException"
    val payload = ManagedPayload(message, methodName, context)
    return OperationsException(payload.setCode(exceptionCode))
}


internal fun <T: CTX> T.operationsException(
    message: String,
    code: ExceptionCode
):OperationsException = operationsException(message, code, this)

