package po.exposify.extensions

import po.exposify.exceptions.InitException
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.initException
import po.exposify.exceptions.operationsException
import po.misc.exceptions.ManagedException
import po.misc.interfaces.IdentifiableContext
import po.misc.types.castOrThrow
import po.misc.types.getOrThrow

internal inline fun <reified T: Any> Any?.castOrOperations(ctx: IdentifiableContext? = null ): T {
   return this.castOrThrow<T, OperationsException>(ctx){
       operationsException(it, ExceptionCode.CAST_FAILURE, ctx)
   }
}

@PublishedApi
internal inline fun <reified T: Any> Any?.castOrInit(ctx: IdentifiableContext? = null): T
{
    return this.castOrThrow<T, InitException>(ctx){
        initException(it, ExceptionCode.CAST_FAILURE)
    }
}

internal inline fun <reified T: Any> Any.castLetOrInit(block: (T)->T): T {
    try {
       val result =  this.castOrThrow<T, InitException>(null){
           initException(it,  ExceptionCode.CAST_FAILURE)
       }
       return block.invoke(result)
    }catch (ex: Throwable){
        throw  ex
    }
}


internal inline fun <reified T : Any> T?.getOrOperations(ctx: IdentifiableContext? = null): T {
   return this.getOrThrow<T, OperationsException>(ctx){
       operationsException(it, ExceptionCode.VALUE_IS_NULL)
   }
}

internal fun <T : Any> T?.getOrOperations(className: String, ctx: IdentifiableContext? = null): T {
    if(this != null){
        return this
    }else{
       throw operationsException("$className is null", ExceptionCode.VALUE_IS_NULL, ctx)
    }
}

@PublishedApi
internal inline fun <reified T : Any> T?.getOrInit(ctx: IdentifiableContext? = null): T {
    return this.getOrThrow<T, InitException>(ctx){
        initException(it, ExceptionCode.VALUE_IS_NULL)
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
