package po.exposify.exceptions

import po.exposify.exceptions.enums.ExceptionCode
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.ManagedException
import po.misc.interfaces.IdentifiableContext

fun  Boolean.trueOrInitException(){
    if(!this){
       val exception = InitException("Tested value is false", ExceptionCode.UNDEFINED, null)
        throw exception
    }
    return
}

@PublishedApi
internal fun initException(payload: ManagedCallSitePayload): OperationsException{
    val exception = OperationsException(payload.message, payload.source as ExceptionCode, payload.cause)
    exception.addHandlingData(payload.ctx, ManagedException.ExceptionEvent.Registered)
    return exception
}

@PublishedApi
internal fun initException(message: String, code: ExceptionCode, original: Throwable): InitException{
    return InitException(message, code, original)
}


@PublishedApi
internal fun initException(message: String, code: ExceptionCode,  ctx: IdentifiableContext? = null): InitException{
    val exception = InitException(message, code, null)
    if(ctx != null){
        exception.addHandlingData(ctx, ManagedException.ExceptionEvent.Registered)
    }
    return exception
}

internal fun initAbnormal(message: String, ctx: IdentifiableContext? = null): InitException{
    val exception = InitException(message, ExceptionCode.ABNORMAL_STATE, null)
    if(ctx != null){
        exception.addHandlingData(ctx, ManagedException.ExceptionEvent.Registered)
    }
    return exception
}
internal fun throwInit(message: String, code: ExceptionCode, ctx: IdentifiableContext? = null): Nothing{
    throw  initException(message, code, ctx)
}


@PublishedApi
internal fun exception(payload: ManagedCallSitePayload): OperationsException = operationsException(payload)


@PublishedApi
internal fun operationsException(payload: ManagedCallSitePayload): OperationsException{
    val exception = OperationsException(payload.message, payload.source as ExceptionCode, payload.cause)
    exception.addHandlingData(payload.ctx, ManagedException.ExceptionEvent.Registered)
    return exception
}

@PublishedApi
internal fun operationsException(message: String, code: ExceptionCode, original: Throwable? = null): OperationsException{
    return OperationsException(message, code, original)
}
