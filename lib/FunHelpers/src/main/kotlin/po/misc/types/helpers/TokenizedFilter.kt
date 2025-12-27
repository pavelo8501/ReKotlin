package po.misc.types.helpers

import po.misc.types.safeCast
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken
import po.misc.types.token.safeCast
import kotlin.collections.filterIsInstance
import kotlin.reflect.KClass



inline fun <reified T: Tokenized<*>> List<Tokenized<*>>.filterTokenized(
    baseClass: KClass<T>,
    token: TypeToken<*>
): List<T>{
    val typeFiltered = mapNotNull { tokenized ->
        tokenized.safeCast(baseClass, token)
    }
   return typeFiltered
}


inline fun <reified T: Tokenized<*>, TT: Any> Collection<Tokenized<*>>.filterTokenHolder(
    paramClass: KClass<TT>
): List<T>{

    val baseClass = T::class
    val filtered = filterIsInstance<T>()
    val result = mutableListOf<T>()
    filtered.forEach {tokenized->
        if(paramClass in tokenized.typeToken.inlinedParameters){
            result.add(tokenized)
        }
    }
    return result
}

inline fun <reified T: Tokenized<TT>, TT: Any> Collection<Tokenized<*>>.filterTokenized(
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


