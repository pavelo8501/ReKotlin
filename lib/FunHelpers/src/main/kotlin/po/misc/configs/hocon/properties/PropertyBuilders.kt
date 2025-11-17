package po.misc.configs.hocon.properties

import po.misc.configs.hocon.models.HoconEntry
import po.misc.configs.hocon.models.HoconListEntry
import po.misc.configs.hocon.models.HoconNestedEntry
import po.misc.configs.hocon.models.HoconObject
import po.misc.configs.hocon.models.HoconPrimitives
import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.models.HoconListPrimitives
import po.misc.types.token.tokenOf
import kotlin.reflect.KClass


inline fun <T: HoconResolvable<T>, reified V>  T.hoconProperty():HoconProperty<T, V> {
    val primitive = HoconPrimitives.resolveTokenToPrimitive(tokenOf<V>())
    return HoconProperty<T, V>(this, HoconEntry<T, V>(this, primitive))
}
inline fun <T: HoconResolvable<T>, reified V>  T.hoconProperty(
    hoconPrimitive:  HoconPrimitives<V>,
):HoconProperty<T, V> {
    val constructed = HoconPrimitives.buildObject(hoconPrimitive, tokenOf<V>())
    return HoconProperty(this, HoconEntry(this, constructed))
}


inline fun <T: HoconResolvable<T>, reified V: Any, R: Any>  T.hoconProperty(
    hoconPrimitive:  HoconPrimitives<V>,
    noinline transformLambda: (V)->R
): HoconTransformProperty<T, V, R> {
    val entry =  HoconEntry<T, V>(this, hoconPrimitive)
    return HoconTransformProperty(this, entry, transformLambda)
}

inline fun <T: HoconResolvable<T>, reified V: Any, R: Any>  T.hoconProperty(
    noinline transformLambda: (V)->R
): HoconTransformProperty<T, V, R> {
    val typeToken = tokenOf<V>()
    val primitive = HoconPrimitives.resolveTokenToPrimitive(typeToken)
    val entry =  HoconEntry<T, V>(this, primitive)
    return HoconTransformProperty(this, entry, transformLambda)
}

inline fun <reified T: HoconResolvable<T>, reified V, reified V1 : List<V>?> T.listProperty():HoconListProperty<T, V> {
    val typeToken =  tokenOf<V>()
    val listTypeToken =  tokenOf<V1>()
    val constructed =  HoconListPrimitives.buildListObject(typeToken, listTypeToken)
    val hoconSourceEntry: HoconEntry<T, V> = HoconEntry(this, constructed.sourcePrimitive)
    val entry = HoconListEntry(this, constructed, hoconSourceEntry)
    return HoconListProperty(this, entry)
}


inline fun <T: HoconResolvable<T>, reified V: HoconResolvable<V>> T.hoconNested(
    nestedClass: V
):HoconNestedProperty<T, V>{
    val typeToken = tokenOf<V>()
    val primitive = HoconObject(typeToken)
    val entry = HoconNestedEntry(this, primitive, nestedClass)
    return HoconNestedProperty(this,entry)
}
