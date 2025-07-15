package po.exposify.extensions

import po.exposify.exceptions.InitException
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.initException
import po.exposify.exceptions.managedPayload
import po.exposify.exceptions.operationsException
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.ManagedException
import po.misc.exceptions.waypointInfo
import po.misc.interfaces.CtxId
import po.misc.interfaces.IdentifiableContext
import po.misc.interfaces.TypedContext
import po.misc.types.castOrManaged
import po.misc.types.castOrThrow
import po.misc.types.castTypedOrThrow
import po.misc.types.getOrThrow
import kotlin.reflect.KClass

internal inline fun <reified T: Any> Any?.castOrOperations(ctx:IdentifiableContext): T {
   return this.castOrThrow<T>(ctx){
       operationsException(ctx.managedPayload(it, ExceptionCode.CAST_FAILURE))
   }
}

internal inline fun <reified T: Any> Any?.castOrOperations(payload: ManagedCallSitePayload): T {
    return this.castOrThrow<T>(payload){msg->
        OperationsException(msg, ExceptionCode.CAST_FAILURE, null)
    }
}

internal fun <T: Any> Any?.castOrOperations(kClass: KClass<T>): T {
    return this.castOrThrow<T>(kClass){msg, th->
        OperationsException(msg, ExceptionCode.CAST_FAILURE, th)
    }
}

internal fun <T: Any> Any?.castOrOperations(kClass: KClass<T>, ctx: CtxId): T {
    return this.castOrThrow<T>(kClass){msg, th->
       val exception = OperationsException(msg, ExceptionCode.CAST_FAILURE, th)
        exception.addHandlingData(ctx, ManagedException.ExceptionEvent.Registered)
    }
}

//internal inline fun <reified C: IdentifiableContext> IdentifiableContext?.castOrOperations(): C {
//
//    if(this is TypedContext<*>){
//        val thisAsTyped = this as TypedContext<C>
//        thisAsTyped.castTypedOrThrow(thisAsTyped){str,th->
//            operationsException(managedPayload(cause = th, source = ExceptionCode.CAST_FAILURE))
//        }
//    }else{
//        castOrThrow<C>(this){
//
//        }
//    }
//
//    return this.castOrThrow(typeData.clazz){
//        operationsException(this.managedPayload(it, ExceptionCode.CAST_FAILURE))
//    }
//}


@PublishedApi
internal inline fun <reified T: Any> Any?.castOrInit(ctx: IdentifiableContext): T
{
    return this.castOrThrow<T>(ctx){
        initException(it, ExceptionCode.CAST_FAILURE)
    }
}

//internal inline fun <reified T: Any> Any.castLetOrInit(block: (T)->T): T {
//    try {
//       val result =  this.castOrThrow<T>(null){
//           initException(it,  ExceptionCode.CAST_FAILURE)
//       }
//       return block.invoke(result)
//    }catch (ex: Throwable){
//        throw  ex
//    }
//}


internal inline fun <reified T : Any> T?.getOrOperations(ctx: IdentifiableContext): T {
   return this.getOrThrow<T>(){
       operationsException(ctx.managedPayload(it, ExceptionCode.VALUE_IS_NULL))
   }
}

internal fun <T : Any> T?.getOrOperations(message: String, ctx: IdentifiableContext? = null): T {
    return this ?: run {
        val message = "Can not get.  Value is null. $message"
        val ex = operationsException(message, ExceptionCode.VALUE_IS_NULL, null)
        if (ctx != null) {
            ex.addHandlingData(ctx, ManagedException.ExceptionEvent.Registered)
        }
        throw ex
    }
}

internal fun <T : Any> T?.getOrOperations(kClass: KClass<T>): T {
    return this ?: run {
        val message = "Can not get ${kClass.simpleName.toString()}. Value is null"
        throw OperationsException("message", ExceptionCode.VALUE_IS_NULL, null)
    }
}



@PublishedApi
internal inline fun <reified T : Any> T?.getOrInit(ctx: IdentifiableContext): T {
    return this.getOrThrow<T>(){
        initException(ctx.managedPayload(message =  it, source =  ExceptionCode.VALUE_IS_NULL))
    }
}

internal fun <T : Any> T?.getOrInit(className: String, ctx: IdentifiableContext? = null): T {
    if(this != null){
        return this
    }else{
        throw initException("$className is null", ExceptionCode.VALUE_IS_NULL, ctx)
    }
}

fun <T: Any?, E: ManagedException> T.testOrThrow(exception : E, predicate: (T) -> Boolean): T{
    if (predicate(this)){
        return this
    }else{
        throw exception
    }
}

inline fun <T: Any> T?.letOrThrow(ex : OperationsException, block: (T)-> T): Unit{
    if(this != null){
        block(this)
    } else {
        throw ex
    }
}
