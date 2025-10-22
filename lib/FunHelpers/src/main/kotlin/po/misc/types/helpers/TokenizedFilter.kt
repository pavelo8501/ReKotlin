package po.misc.types.helpers

import po.misc.types.safeCast
import po.misc.types.token.Tokenized
import kotlin.reflect.KClass


inline fun <reified T: Tokenized<TT>, TT: Any> List<Tokenized<*>>.filterTokenized(
    paramClass: KClass<TT>
): List<T>{

    val filtered = filterIsInstance<Tokenized<TT>>()
    val typeFiltered = filtered.mapNotNull {tokenHolder->
        if(tokenHolder.typeToken == paramClass){
            tokenHolder.safeCast<T>()
        }else{
            null
        }
    }
    return typeFiltered
}