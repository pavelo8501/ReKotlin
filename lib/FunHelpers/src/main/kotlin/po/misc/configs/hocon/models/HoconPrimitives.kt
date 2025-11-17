package po.misc.configs.hocon.models

import com.typesafe.config.ConfigValueType
import po.misc.configs.hocon.HoconResolvable
import po.misc.context.tracable.TraceableContext
import po.misc.reflection.primitives.BooleanClass
import po.misc.reflection.primitives.IntClass
import po.misc.reflection.primitives.LongClass
import po.misc.reflection.primitives.PrimitiveClass
import po.misc.reflection.primitives.StringClass
import po.misc.reflection.primitives.WildCardClass
import po.misc.types.safeCast
import po.misc.types.token.TypeToken


sealed interface HoconPrimitives<V> {
    val primitiveClass: PrimitiveClass<*>
    val hoconType: ConfigValueType
    val typeToken: TypeToken<V>

    companion object : TraceableContext {

        fun <V> resolveTokenToPrimitive(
            typeToken: TypeToken<V>
        ): HoconPrimitives<V> {
            @Suppress("UNCHECKED_CAST")
            return when (typeToken.kClass) {
                String::class -> HoconString(typeToken as TypeToken<String>) as HoconPrimitives<V>
                Long::class -> HoconLong(typeToken as TypeToken<Long>) as HoconPrimitives<V>
                Int::class -> HoconInt(typeToken as TypeToken<Int>) as HoconPrimitives<V>
                Boolean::class -> HoconBoolean(typeToken as TypeToken<Boolean>) as HoconPrimitives<V>
                else -> throw IllegalArgumentException("ssss")
            }
        }

       inline fun <reified V> buildObject(
            companion: HoconPrimitives<V>,
            typeToken: TypeToken<V>
        ): HoconPrimitives<V> {
           @Suppress("UNCHECKED_CAST")
           when(companion){
                is HoconString.Companion  -> {
                    val casted = typeToken.safeCast<TypeToken<String>>()
                    if(casted != null){
                      return  HoconString(casted) as HoconPrimitives<V>
                    }
                }
                is  HoconBoolean.Companion -> {
                    val casted = typeToken.safeCast<TypeToken<Boolean>>()
                    if(casted != null){
                        return  HoconBoolean(casted) as HoconPrimitives<V>
                    }
                }
                is  HoconLong.Companion -> {
                    val casted = typeToken.safeCast<TypeToken<Long>>()
                    if(casted != null){
                        return  HoconLong(casted) as HoconPrimitives<V>
                    }
                }
                is HoconInt.Companion ->{
                    val casted = typeToken.safeCast<TypeToken<Int>>()
                    if(casted != null){
                        return  HoconInt(casted) as HoconPrimitives<V>
                    }
                }
               else -> {
                   TODO("Not yet supported")
               }
            }
           TODO("Not yet supported")
        }

    }
}

sealed interface HoconListPrimitives<V, V1 : List<V>?> : HoconPrimitives<V> {
    val listTypeToken: TypeToken<V1>
    val sourcePrimitive: HoconPrimitives<V>
    override val hoconType: ConfigValueType

    override val typeToken: TypeToken<V> get() = sourcePrimitive.typeToken

    companion object: TraceableContext{
        fun <V, V1 : List<V>?> buildListObject(typeToken: TypeToken<V>, listTypeToken: TypeToken<V1>): HoconList<V, V1> {
            val primitive = HoconPrimitives.resolveTokenToPrimitive(typeToken)
            return  HoconList(listTypeToken , primitive)
        }
    }
}


class HoconString(
    override val typeToken: TypeToken<String> = TypeToken.create()
):  HoconPrimitives<String>{

    override val primitiveClass: StringClass = Companion.primitiveClass
    override val hoconType:  ConfigValueType = Companion.hoconType

    companion object: HoconPrimitives<String>{
        override val typeToken:TypeToken<String> = TypeToken.create()
        override val primitiveClass: StringClass = StringClass
        override val hoconType:  ConfigValueType = ConfigValueType.STRING
    }
}


class HoconInt(
    override val typeToken: TypeToken<Int> = TypeToken.create()
): HoconPrimitives<Int>{

    override val primitiveClass: IntClass = Companion.primitiveClass
    override val hoconType:  ConfigValueType = Companion.hoconType

    companion object: HoconPrimitives<Int>{
        override val typeToken:TypeToken<Int> = TypeToken.create()
        override val primitiveClass: IntClass = IntClass
        override val hoconType:  ConfigValueType = ConfigValueType.NUMBER
    }
}

class HoconLong(
    override val typeToken: TypeToken<Long> = TypeToken.create()
) : HoconPrimitives<Long>{

    override val primitiveClass: LongClass = Companion.primitiveClass
    override val hoconType:  ConfigValueType = Companion.hoconType

    companion object:  HoconPrimitives<Long>{
        override val primitiveClass: LongClass = LongClass
        override val hoconType:  ConfigValueType = ConfigValueType.NUMBER
        override val typeToken: TypeToken<Long> = TypeToken.create()
    }
}

class HoconBoolean(
    override val typeToken: TypeToken<Boolean> = TypeToken.create()
) : HoconPrimitives<Boolean>{

    override val primitiveClass: BooleanClass = BooleanClass
    override val hoconType:  ConfigValueType = ConfigValueType.BOOLEAN

    companion object: HoconPrimitives<Boolean>{
        override val primitiveClass: BooleanClass = BooleanClass
        override val hoconType:  ConfigValueType = ConfigValueType.BOOLEAN
        override val typeToken: TypeToken<Boolean> = TypeToken.create()
    }
}

class HoconObject<V: HoconResolvable<V>>(
    override val typeToken: TypeToken<V>
) : WildCardClass<V>(typeToken), HoconPrimitives<V>{
    override val primitiveClass: WildCardClass<V> = this
    override val hoconType:  ConfigValueType = ConfigValueType.OBJECT
    companion object{
        val hoconType:  ConfigValueType = ConfigValueType.OBJECT
    }
}

class HoconList<V, V1 : List<V>?>(
    override val listTypeToken: TypeToken<V1>,
    override val sourcePrimitive:  HoconPrimitives<V>
):  HoconListPrimitives<V, V1>{
    override val hoconType: ConfigValueType = ConfigValueType.LIST
    override val primitiveClass = sourcePrimitive.primitiveClass

    companion object {
        val hoconType: ConfigValueType = ConfigValueType.LIST
    }
}
