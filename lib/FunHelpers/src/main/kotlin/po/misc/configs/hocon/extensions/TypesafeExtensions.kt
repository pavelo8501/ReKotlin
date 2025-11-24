package po.misc.configs.hocon.extensions

import com.typesafe.config.Config
import com.typesafe.config.ConfigValue
import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.models.HoconEntryBase
import po.misc.configs.hocon.models.HoconListEntry
import po.misc.exceptions.managedException
import po.misc.types.castOrThrow
import po.misc.types.k_class.simpleOrAnon

fun <C: HoconResolvable<C>,  V> HoconEntryBase<C,  V>.parseValue(rawValue: ConfigValue):V {
    val errorMessage =  "$componentName can not be cast to ${ valueTypeToken.kClass.simpleOrAnon}"
    val unwrapped = rawValue.unwrapped()
    val casted = unwrapped.castOrThrow(this, hoconPrimitive.typeToken.kClass){
        throw managedException(errorMessage)
    }
    return casted
}

fun <C: HoconResolvable<C>,  V> HoconEntryBase<C,  V>.parseNumericValue(config: Config): V {
    return when(hoconPrimitive.typeToken.kClass){
         Long::class -> config.getLong(componentName).castOrThrow(hoconPrimitive.typeToken.kClass)
         Int::class -> config.getInt(componentName).castOrThrow(hoconPrimitive.typeToken.kClass)
         Double::class -> config.getDouble(componentName).castOrThrow(hoconPrimitive.typeToken.kClass)
         else -> throw managedException("Else branch reached when parsing numeric value")
    }
}

fun <C: HoconResolvable<C>,  V> HoconListEntry<C,  V>.parseListValue(rawValue: ConfigValue): List<V> {
    val errorMessage =  "$componentName can not be cast to ${ valueTypeToken.kClass.simpleOrAnon}"
    val unwrapped = rawValue.unwrapped()
    val casted = unwrapped.castOrThrow<List<V>>(this){
        throw managedException(errorMessage)
    }
    return casted
}
