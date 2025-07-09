package po.misc.types

import po.misc.exceptions.ManagedException
import po.misc.interfaces.IdentifiableContext
import kotlin.reflect.KClass


fun <T : Any> filterByType(
     typeData: Typed<T>,
     list:  List<Any>
): List<T> {
    return list.mapNotNull {it.safeCast(typeData.kClass) }
}


fun <T : Any>  List<Any>.findByTypeFirstOrNull(
    typeData: Typed<T>,
):T? {
    return firstNotNullOfOrNull { it.safeCast(typeData.kClass) }
}


inline fun <T : Any> List<*>.castListOrThrow(
    kClass: KClass<T>,
    ctx: IdentifiableContext,
    exceptionProvider: (message: String, original: Throwable)-> Throwable,
): List<T> {
    return this.mapNotNull { it.castOrThrow<T>(kClass, exceptionProvider) }
}


fun <T : Any> List<*>.castListOrManaged(
    kClass: KClass<T>,
): List<T> {
    return this.mapNotNull { it.castOrManaged<T>(kClass) }
}


inline fun <reified BASE : Any, reified E : ManagedException> Any?.castBaseOrThrow(
    ctx: IdentifiableContext? = null,
    exceptionProvider: (message: String)-> E,
): BASE {
    try {
        return this as BASE
    }catch (ex: Throwable){
        val message = if (this == null) {
            "Cannot cast null to ${BASE::class.simpleName}"
        }else{
            "Cannot cast ${this::class.simpleName} to ${BASE::class.simpleName}"
        }
        val exception = exceptionProvider(message)
        if(ctx != null){
            exception.throwSelf(ctx)
        }else{
            throw exception
        }
    }
}
