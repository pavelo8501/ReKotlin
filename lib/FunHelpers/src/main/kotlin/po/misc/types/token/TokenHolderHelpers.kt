package po.misc.types.token

import po.misc.types.safeCast
import kotlin.reflect.KClass


fun <T: TokenHolder, P> TokenHolder.safeCast(
    kClass: KClass<T>,
    typeToken: TypeToken<P>
):T?{
    val casted = this.safeCast<T>(kClass)
    if(casted != null && typeToken == casted.typeToken){
        return casted
    }
    return null
}

inline fun <reified T: TokenHolder, P>  Iterable<TokenHolder>.filterTokenized(
    typeToken: TypeToken<P>,
): List<T>{
    return mapNotNull {
        it.safeCast(T::class, typeToken)
    }
}

fun <T: TokenHolder, P>  Iterable<TokenHolder>.filterTokenized(
    kClass: KClass<T>,
    typeToken: TypeToken<P>,
): List<T>{
    return mapNotNull { it.safeCast(kClass, typeToken) }
}



inline fun <reified T: TokenizedResolver<RT, VT>, RT, VT>  Iterable<TokenizedResolver<*, *>>.filterTokenized(
    receiverType: TypeToken<RT>,
    valueType: TypeToken<VT>,
): List<T>{
    val filteredByTypes = mutableListOf<TokenizedResolver<*, *>>()
    for (element in this){
        if(element.receiverType == receiverType && element.valueType == valueType){
            filteredByTypes.add(element)
        }
    }
    return filteredByTypes.filterIsInstance<T>()
}



