package po.misc.types

import po.misc.data.output.output
import po.misc.data.styles.Colour
import po.misc.exceptions.throwableToText
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass


fun <T: Any> KClass<*>.safeClassCast(token: TypeToken<T>):KClass<T>?{
    if(token.strictEquality(this)){
       return try {
            this.castOrThrow()
        }catch (th: Throwable){
            "KClass<*>.safeCast throws : ${th.throwableToText()}".output(Colour.YellowBright)
            null
        }
    }
    return null
}