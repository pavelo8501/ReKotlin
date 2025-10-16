package po.misc.configs.hocon

import com.typesafe.config.Config
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.reflection.primitives.BooleanClass
import po.misc.reflection.primitives.IntClass
import po.misc.reflection.primitives.StringClass
import po.misc.types.castOrThrow
import po.misc.types.helpers.filterByType
import po.misc.types.helpers.simpleOrAnon
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass


interface HoconResolvable<T: Any>{
    val resolver: HoconConfigResolver<T>
    
}

class HoconConfigResolver<T: Any>(
    val typeToken: TypeToken<T>
){

    internal val members = mutableListOf<HoconConfigResolver<*>>()
    internal  val entries = mutableListOf<HoconEntryBase<T, *>>()

    fun registerMember(resolvable: HoconResolvable<*>): HoconConfigResolver<T>{
        members.add(resolvable.resolver)
        "Resolver of Member Class ${resolvable::class.simpleOrAnon}  Registered".output(Colour.CyanBright)
        return this
    }

    fun register(hoconEntry: HoconEntryBase<T, *>): Boolean{
        entries.add(hoconEntry)
        return true
    }

    fun readConfig(receiver: T, hoconFactory: Config){
        entries.forEach {hoconEntry->
            "readConfig for for class ${receiver::class.simpleOrAnon}. Processing entry $hoconEntry".output(Colour.Green)
            when(hoconEntry){
                is HoconEntry->{
                    hoconEntry.readConfig(receiver, hoconFactory)
                }
                is HoconNullableEntry ->{
                    hoconEntry.readConfig(receiver, hoconFactory)
                }
                is HoconListEntry ->{
                    hoconEntry.readConfig(receiver, hoconFactory)
                }
                is HoconNestedEntry<*, *> ->{
                    hoconEntry.readConfig(receiver, hoconFactory)
                }
            }
        }
    }
}



fun <T: Any> Config.parseValue(name: String, hoconPrimitive: HoconPrimitives<T>):T{

    return when(hoconPrimitive){
        is HoconBoolean ->{
            getBoolean(name).castOrThrow<T>(hoconPrimitive, hoconPrimitive.primitiveClass.kClass){
                Exception("$name can not be cast to ${ hoconPrimitive.primitiveClass.kClass.simpleOrAnon}")
            }
        }
        is HoconNumber->{
            getNumber(name).castOrThrow<T>(hoconPrimitive,  hoconPrimitive.primitiveClass.kClass){
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

fun <T: HoconResolvable<T>> T.readConfig(factory:  Config){
    resolver.readConfig(this, factory)
}

inline fun <reified T: Any> HoconResolvable<T>.createResolver():HoconConfigResolver<T>{
    return HoconConfigResolver(TypeToken.create<T>())
}
