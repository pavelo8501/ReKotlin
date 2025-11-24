package po.misc.collections

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


fun <T: Any> List<T>.exactlyOneOrThrow(exceptionProvider:()-> Throwable):T{
    if(size != 1){
       throw exceptionProvider()
    }else{
        return this[0]
    }
}

fun <T: Any> T?.asList(): List<T>{

   return if (this != null){
        listOf(this)
    }else{
        emptyList<T>()
    }
}


/**
 * Attempts to insert the given [value] under [key] if the key is not already present.
 *
 * If the key is absent:
 *  - The [value] is inserted and returned.
 *
 * If the key already exists:
 *  - The existing value is passed to [failureAction], and the result of that callback
 *    is returned instead.
 *
 * This helper enables "conditional overwrite" patterns, where the caller decides
 * whether an existing mapping should be kept, replaced, or ignored.
 *
 * @param key The key to check.
 * @param value The value to insert if no existing mapping is found.
 * @param failureAction Called when the key already exists; receives the existing value
 *                      and may return an alternative value or `null`.
 * @return The inserted value, or the result of [failureAction] for existing entries.
 */
fun <K: Any, V: Any>  MutableMap<K, V>.putIfAbsentOr(key: K, value:V, failureAction: (V)-> V?):V?{
    val existentValue = get(key)
    
    if (existentValue == null) {
        put(key, value)
        return value
    } else {

        return failureAction.invoke(existentValue)
    }
}



