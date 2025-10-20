package po.misc.configs.hocon.extensions

import com.typesafe.config.Config
import po.misc.configs.hocon.models.HoconBoolean
import po.misc.configs.hocon.models.HoconGenericList
import po.misc.configs.hocon.models.HoconInt
import po.misc.configs.hocon.models.HoconLong
import po.misc.configs.hocon.models.HoconPrimitives
import po.misc.configs.hocon.models.HoconString
import po.misc.types.castOrThrow
import po.misc.types.helpers.filterByType
import po.misc.types.helpers.simpleOrAnon


fun <T: Any> Config.parseValue(name: String, hoconPrimitive: HoconPrimitives<T>):T{
    return when(hoconPrimitive){
        is HoconBoolean ->{
            getBoolean(name).castOrThrow<T>(hoconPrimitive, hoconPrimitive.primitiveClass.kClass){
                Exception("$name can not be cast to ${ hoconPrimitive.primitiveClass.kClass.simpleOrAnon}")
            }
        }
        is HoconInt ->{
            getNumber(name).castOrThrow<T>(hoconPrimitive,  hoconPrimitive.primitiveClass.kClass){
                Exception("$name can not be cast to ${hoconPrimitive.primitiveClass.kClass.simpleOrAnon}")
            }
        }
        is HoconLong ->{
            getLong(name).castOrThrow<T>(hoconPrimitive,  hoconPrimitive.primitiveClass.kClass){
                Exception("$name can not be cast to ${hoconPrimitive.primitiveClass.kClass.simpleOrAnon}")
            }
        }
        is HoconString ->{
            getString(name).castOrThrow<T>(hoconPrimitive,  hoconPrimitive.primitiveClass.kClass){
                Exception("$name can not be cast to ${ hoconPrimitive.primitiveClass.kClass.simpleOrAnon}")
            }
        }
        else -> {
            TODO("Testing not supported")
        }
    }
}

fun <T: Any> Config.parseList(name: String,  hoconPrimitive: HoconPrimitives<T>): List<T>{
    return when(hoconPrimitive) {
        is HoconGenericList -> {
            val list = getStringList(name)
            list.filterByType(hoconPrimitive.typeToken)
        }
        else -> {
            TODO("Testing not supported")
        }
    }
}