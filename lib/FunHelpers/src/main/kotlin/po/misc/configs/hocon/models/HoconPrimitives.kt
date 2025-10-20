package po.misc.configs.hocon.models

import com.typesafe.config.ConfigValueType
import po.misc.reflection.primitives.BooleanClass
import po.misc.reflection.primitives.IntClass
import po.misc.reflection.primitives.LongClass
import po.misc.reflection.primitives.PrimitiveClass
import po.misc.reflection.primitives.StringClass
import po.misc.reflection.primitives.WildCardClass
import po.misc.types.token.TypeToken


sealed interface HoconPrimitives<V: Any>{
    val primitiveClass: PrimitiveClass<V>
    val hoconType:  ConfigValueType
    val typeToken: TypeToken<V>
}

object HoconString : HoconPrimitives<String>{
    override val primitiveClass: StringClass = StringClass
    override val hoconType:  ConfigValueType = ConfigValueType.STRING
    override val typeToken: TypeToken<String> = TypeToken.create()
}

object HoconInt : HoconPrimitives<Int>{
    override val primitiveClass: IntClass = IntClass
    override val hoconType:  ConfigValueType = ConfigValueType.NUMBER
    override val typeToken: TypeToken<Int> = TypeToken.create()
}


object HoconLong : HoconPrimitives<Long>{
    override val primitiveClass: LongClass = LongClass
    override val hoconType:  ConfigValueType = ConfigValueType.NUMBER
    override val typeToken: TypeToken<Long> = TypeToken.create()
}


object HoconBoolean : HoconPrimitives<Boolean>{
    override val primitiveClass: BooleanClass = BooleanClass
    override val hoconType:  ConfigValueType = ConfigValueType.BOOLEAN
    override val typeToken: TypeToken<Boolean> = TypeToken.create()
}

object HoconObject : HoconPrimitives<Any>{
    override val primitiveClass: WildCardClass = WildCardClass
    override val hoconType:  ConfigValueType = ConfigValueType.OBJECT
    override val typeToken: TypeToken<Any> = TypeToken.create()
}

class HoconGenericList<V: Any>(
    override val primitiveClass: PrimitiveClass<V>,
    override val typeToken: TypeToken<V>
):  HoconPrimitives<V>{
    override val hoconType: ConfigValueType = ConfigValueType.LIST

}

object HoconList : HoconPrimitives<String>{
    override val primitiveClass: StringClass = StringClass
    override val hoconType:  ConfigValueType = ConfigValueType.LIST
    override val typeToken: TypeToken<String> = TypeToken.create()
}

object HoconNullable