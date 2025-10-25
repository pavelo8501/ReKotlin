package po.misc.configs.hocon.extensions

import com.typesafe.config.ConfigValue
import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.models.HoconEntryBase
import po.misc.configs.hocon.models.HoconListEntry
import po.misc.context.component.managedException
import po.misc.types.castOrThrow
import po.misc.types.helpers.simpleOrAnon

fun <C: HoconResolvable<C>,  V> HoconEntryBase<C,  V>.parseValue(rawValue: ConfigValue):V {
    val errorMessage =  "$name can not be cast to ${ valueTypeToken.kClass.simpleOrAnon}"
    val unwrapped = rawValue.unwrapped()
    val casted = unwrapped.castOrThrow(this, hoconPrimitive.typeToken.kClass){
        throw managedException(errorMessage)
    }
    return casted
}
//hoconPrimitive.primitiveClass.kClass.simpleOrAnon
fun <C: HoconResolvable<C>,  V> HoconListEntry<C,  V>.parseListValue(rawValue: ConfigValue): List<V> {
    val errorMessage =  "$name can not be cast to ${ valueTypeToken.kClass.simpleOrAnon}"
    val unwrapped = rawValue.unwrapped()
    val casted = unwrapped.castOrThrow<List<V>>(this){
        throw managedException(errorMessage)
    }
    return casted
}
