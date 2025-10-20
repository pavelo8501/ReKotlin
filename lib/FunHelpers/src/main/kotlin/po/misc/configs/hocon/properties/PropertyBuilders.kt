package po.misc.configs.hocon.properties

import po.misc.configs.hocon.models.HoconEntry
import po.misc.configs.hocon.models.HoconGenericList
import po.misc.configs.hocon.models.HoconListEntry
import po.misc.configs.hocon.models.HoconNestedEntry
import po.misc.configs.hocon.models.HoconNullable
import po.misc.configs.hocon.models.HoconNullableEntry
import po.misc.configs.hocon.models.HoconObject
import po.misc.configs.hocon.models.HoconPrimitives
import po.misc.configs.hocon.HoconResolvable
import po.misc.reflection.primitives.PrimitiveClass
import po.misc.reflection.primitives.lookupPrimitive
import po.misc.types.token.TypeToken


inline fun <T: HoconResolvable<T>, reified V: Any>  T.hoconProperty(
    hoconPrimitive:  HoconPrimitives<V>,
    mandatory: Boolean = true
):HoconProperty<T, V> {
    val entry =  HoconEntry<T, V>(this, hoconPrimitive, mandatory)
    val prop = HoconProperty<T, V>(this,entry,  hoconPrimitive)
    return prop
}

inline fun <T: HoconResolvable<T>, reified V: Any>  T.nullableProperty(
    hoconPrimitive:  HoconPrimitives<V>,
):HoconNullableProperty<T, V> {
    val entry = HoconNullableEntry<T, V>(this, hoconPrimitive)
    val prop = HoconNullableProperty<T, V>(this, entry,  hoconPrimitive)
    return prop
}

inline fun <T: HoconResolvable<T>, reified V: Any>  T.hoconProperty(
    nullable: HoconNullable,
    hoconPrimitive:  HoconPrimitives<V>,
):HoconNullableProperty<T, V> = nullableProperty(hoconPrimitive)

inline fun <T: HoconResolvable<T>, reified V: Any, R: Any>  T.hoconProperty(
    hoconPrimitive:  HoconPrimitives<V>,
    noinline transformLambda: (V)->R
): HoconTransformProperty<T, V, R> {
    val entry =  HoconEntry<T, V>(this, hoconPrimitive, true)
    val prop = HoconTransformProperty<T, V, R>(this, entry, hoconPrimitive, transformLambda)
    return prop
}

inline fun <T: HoconResolvable<T>, reified V: Any, R: Any>  T.propertyTransforming(
    hoconPrimitive:  HoconPrimitives<V>,
    noinline transformLambda: (V)->R
): HoconTransformProperty<T, V, R> = hoconProperty(hoconPrimitive, transformLambda)



inline fun <reified T: HoconResolvable<T>, reified V: Any>  T.hoconList():HoconListProperty<T, V> {
    val primitive = PrimitiveClass.lookupPrimitive<V>()
    val genericList : HoconGenericList<V> = HoconGenericList(
        primitive,
        TypeToken.create<V>()
    )
    val entry = HoconListEntry<T, V>(this, genericList)
    val prop = HoconListProperty<T, V>(this, entry, genericList)
    return prop
}


inline fun <T: HoconResolvable<T>, reified V: HoconResolvable<V>> T.hoconNested(
    nestedClass: V,
):HoconNestedProperty<T, V> {
    resolver.registerMember(nestedClass)
    val entry = HoconNestedEntry(this, HoconObject, nestedClass)
    val prop = HoconNestedProperty<T, V>(this,entry,  HoconObject, nestedClass, TypeToken.create<V>())
    return prop
}


