package po.exposify.exceptions

import po.exposify.exceptions.enums.ExceptionCode
import po.misc.context.CTX
import po.misc.exceptions.ExceptionPayload
import po.misc.exceptions.toPayload


@PublishedApi
internal fun initException(payload: ExceptionPayload): InitException{
    val exception = InitException(payload)
    return exception
}

@PublishedApi
internal fun initException(message: String, exceptionCode: ExceptionCode, context: CTX): InitException{
    val payload = context.toPayload{
        message(message)
        code = exceptionCode
    }
    return InitException(payload)
}

@PublishedApi
internal fun <T: CTX> T.initException(
    message: String,
    code: ExceptionCode
):InitException = initException(message, code, this)



internal fun initAbnormal(message: String, context: CTX): InitException{
    val payload = context.toPayload{
        message(message)
        code = ExceptionCode.ABNORMAL_STATE
    }
    return InitException(payload)
}

@PublishedApi
internal fun operationsException(payload: ExceptionPayload): OperationsException{
    return OperationsException(payload)
}

@PublishedApi
internal fun operationsException(message: String, exceptionCode: ExceptionCode,  context: CTX): OperationsException{
    val payload = context.toPayload{
        message(message)
        code = exceptionCode
    }
    return OperationsException(payload)
}

internal fun <T: CTX> T.operationsException(
    message: String,
    code: ExceptionCode
):OperationsException = operationsException(message, code, this)

