package po.exposify.extensions

import po.exposify.exceptions.InitException
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.initException
import po.exposify.exceptions.operationsException
import po.misc.context.CTX
import po.misc.exceptions.ExceptionPayload
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.ManagedException
import po.misc.exceptions.ManagedPayload
import po.misc.exceptions.toPayload
import po.misc.types.castOrThrow
import po.misc.types.getOrManaged
import po.misc.types.getOrThrow
import kotlin.reflect.KClass


internal inline fun <reified T: Any> Any?.castOrOperations(ctx: CTX): T {
   return this.castOrThrow<T>(ctx){message->
       ctx.operationsException(message, ExceptionCode.CAST_FAILURE)
   }
}

internal inline fun <reified T: Any> Any?.castOrOperations(payload: ExceptionPayload): T {
    return this.castOrThrow<T>(payload){msg->
        OperationsException(payload)
    }
}

internal fun <T: Any> Any?.castOrOperations(kClass: KClass<T>): T {
    return this.castOrThrow<T>(kClass){msg, th->
        Exception(msg)
    }
}

@PublishedApi
internal inline fun <reified T: Any> Any?.castOrInit(context: CTX): T
{
    return this.castOrThrow<T>(context){
        context.initException(it, ExceptionCode.CAST_FAILURE)

    }
}


internal inline fun <reified T : Any> T?.getOrOperations(context: CTX): T {
   return this.getOrThrow<T>(){
       context.operationsException(it, ExceptionCode.VALUE_IS_NULL)
   }
}

internal fun <T : Any> T?.getOrOperations(message: String, context: CTX): T {
    return this ?: run {
        throw context.operationsException("Can not get.  Value is null. $message", ExceptionCode.VALUE_IS_NULL)
    }
}

internal fun <T : Any> T?.getOrOperations(kClass: KClass<T>): T {
    return this ?: run {
        throw Exception("Can not get ${kClass.simpleName.toString()}. Value is null")
    }
}

internal fun <T : Any> T?.getOrOperations(payload: ExceptionPayload): T {
    return this ?: run {
        throw OperationsException(payload)
    }
}

@PublishedApi
internal inline fun <reified T : Any> T?.getOrInit(ctx: CTX): T {
    return this.getOrThrow<T>(){
        ctx.initException(it, ExceptionCode.VALUE_IS_NULL)
    }
}

internal fun <T : Any> T?.getOrInit(className: String, ctx: CTX? = null): T {
    if(this != null){
        return this
    }else{
        throw ctx?.initException("$className is null", ExceptionCode.VALUE_IS_NULL)?: run {
            throw Exception("$className is null")
        }
    }
}



internal fun <T : Any> T?.getOrInit(payload: ExceptionPayload): T {
    return this ?: run {
        throw InitException(payload)
    }
}

internal fun <T : Any> T?.getOrInit(
    callingContext: Any
): T = getOrManaged(callingContext){payload-> InitException(payload) }

