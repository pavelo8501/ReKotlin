package po.exposify.extensions

import po.exposify.exceptions.InitException
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.initException
import po.exposify.exceptions.operationsException
import po.misc.context.CTX
import po.misc.types.TypeData
import po.misc.types.castOrThrow
import po.misc.types.getOrThrow
import kotlin.reflect.KClass


@PublishedApi
internal fun <T: Any> Any?.castOrOperations(
    kClass: KClass<T>,
    callingContext: CTX
): T {
    return this.castOrThrow(kClass, callingContext){payload->
        OperationsException(payload)
    }
}

internal inline fun <reified T: Any> Any?.castOrOperations(
    callingContext: CTX
): T  = castOrOperations(T::class, callingContext)

internal fun <T : Any> T?.getOrOperations(
    kClass: KClass<T>,
    callingContext: CTX
): T {
    return  getOrThrow(kClass, callingContext){payload->
        operationsException(payload.setCode(ExceptionCode.VALUE_IS_NULL))
    }
}

internal fun <T : Any> T?.getOrOperations(
    typeData: TypeData<T>,
    callingContext: CTX
): T {
    return  getOrThrow(typeData.kClass, callingContext){payload->
        operationsException(payload.setCode(ExceptionCode.VALUE_IS_NULL))
    }
}

internal inline fun <reified T : Any> T?.getOrOperations(
    callingContext: CTX
): T = getOrOperations(T::class, callingContext)


internal fun <T: Any> Any?.castOrInit(
    kClass: KClass<T>,
    callingContext: CTX
): T {
    return this.castOrThrow(kClass, callingContext){payload->
        InitException(payload)
    }
}

internal inline fun <reified T: Any> Any?.castOrInit(
    callingContext: CTX
): T = castOrInit(T::class, callingContext)


internal fun <T : Any> T?.getOrInit(
    kClass: KClass<T>,
    callingContext: CTX
): T {
    return  getOrThrow(kClass, callingContext){payload->
        initException(payload.setCode(ExceptionCode.VALUE_IS_NULL))
    }
}

internal inline fun <reified T : Any> T?.getOrInit(
    callingContext: CTX
): T = getOrInit(T::class, callingContext)

