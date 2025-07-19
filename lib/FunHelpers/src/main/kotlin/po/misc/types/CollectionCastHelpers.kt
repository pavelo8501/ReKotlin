package po.misc.types

import po.misc.exceptions.ManagedException
import po.misc.context.CTX
import po.misc.context.Identifiable
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
    producer: CTX? = null,
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
        if(producer != null){
            exception.throwSelf(producer, ManagedException.ExceptionEvent.Thrown)
        }else{
            throw exception
        }
    }
}

inline fun <reified T> Iterable<T>.countEqualsOrException(equalsTo: Int, exception:ManagedException):Iterable<T>{
    val actualCount = this.count()
    if(actualCount != equalsTo){
        throw exception
    }else{
        return this
    }
}

