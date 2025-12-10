package po.misc.types.token

import po.misc.types.k_class.typeSlots
import po.misc.types.safeCast


inline fun <reified T: Any> Any.safeParametrizedCast(
    token: TypeToken<*>
):T? {
    val casted = safeCast<T>()
    if(casted != null){
        val kClass = T::class
        val typeSlots = kClass.typeSlots()
        val slotClasses = typeSlots.map { it.kClass }
        if(token.kClass in slotClasses){
            return casted
        }
    }
    return null
}


fun <T: Any> Any.safeCast(
    token: TypeToken<T>
):T? {
    if(token.strictEquality(this::class)){
        return safeCast<T>(token.kClass)
    }
    return null
}
