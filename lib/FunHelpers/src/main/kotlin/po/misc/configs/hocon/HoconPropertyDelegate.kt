package po.misc.configs.hocon

import po.misc.context.Component
import po.misc.data.helpers.output
import po.misc.data.logging.Verbosity
import po.misc.data.styles.Colour
import po.misc.exceptions.managedException
import po.misc.reflection.primitives.PrimitiveClass
import po.misc.reflection.primitives.lookupPrimitive
import po.misc.types.getOrThrow
import po.misc.types.token.TypeToken
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty



sealed class HoconDelegateBase<T: HoconResolvable<T>, V: Any, R>(
   protected val receiver:  T,
   protected val typeToken: TypeToken<V>
): Component,  ReadOnlyProperty<T, R> {

    override var verbosity: Verbosity = Verbosity.Info
    var resolvedProperty: KProperty<*>? = null
        protected set

    protected val resolver : HoconConfigResolver<T> get() = receiver.resolver
    abstract val hoconEntry: HoconEntryBase<T, V>
    var value: V? = null
        protected set

    protected val propertyBacking: KProperty<*> get() = resolvedProperty.getOrThrow(this){
        it.methodName = "propertyBacking"
        managedException(it.message)
    }
    protected val nameToUse: String get() {
        return  resolvedProperty?.name?: "Unresolved"
    }

    operator fun provideDelegate(thisRef: T, property: KProperty<*>): HoconDelegateBase<T, V, R> {
        resolvedProperty = property
        hoconEntry.initialize(property)
        hoconEntry.valueAvailable {
            value = it
        }
        return this
    }
}

class HoconTransformProperty<T: HoconResolvable<T>, V: Any, R: Any>(
    receiver:  T,
    override val hoconEntry:  HoconEntry<T, V>,
    val hoconPrimitive:  HoconPrimitives<V>,
    val transformLambda: (V)->R
): HoconDelegateBase<T, V, R>(receiver, hoconPrimitive.typeToken){

    override val componentName: String get() =  "HoconProperty"

    private val valueRequest : V  get() = value.getOrThrow {
        val msg = "value accessed before initialization. $componentName"
        managedException(msg)
    }

    private var resultBacking:R? = null
    val result:R  get() =  resultBacking?:transformLambda(valueRequest)

    override fun getValue(thisRef: T, property: KProperty<*>): R {
        val resolvedValue = value.getOrThrow {
            val msg = "value accessed before initialization. $componentName"
            managedException(msg)
        }
        val successMsg = "value ${resolvedValue}<${typeToken.typeName}> successfully returned for ${property.name}"
        successMsg.output(Colour.Green)
        return transformLambda.invoke(resolvedValue).let {
            resultBacking = it
            it
        }
    }
}

class HoconProperty<T: HoconResolvable<T>, V: Any>(
    receiver:  T,
    override val hoconEntry: HoconEntry<T, V>,
    val hoconPrimitive:  HoconPrimitives<V>,
): HoconDelegateBase<T, V, V>(receiver, hoconPrimitive.typeToken){

    override val componentName: String get() =  "HoconProperty<T, ${typeToken.typeName}>[${nameToUse}]"
    override fun getValue(thisRef: T, property: KProperty<*>): V {
       val resolvedValue = value.getOrThrow {
            val msg = "value accessed before initialization. $componentName"
            managedException(msg)
        }
        "value ${resolvedValue.toString()}<${typeToken.typeName}> successfully returned for ${property.name}".output(Colour.Green)
        return resolvedValue
    }
}

class HoconNullableProperty<T: HoconResolvable<T>, V: Any>(
    receiver: T,
    override val hoconEntry: HoconNullableEntry<T, V>,
    val  hoconPrimitive:  HoconPrimitives<V>,
): HoconDelegateBase<T,  V, V?>(receiver, hoconPrimitive.typeToken) {

    override val componentName: String get() =  "HoconNullableProperty<T, ${typeToken.typeName}>[name: ${nameToUse}]"
    val nullable: HoconNullable = HoconNullable

    override fun getValue(thisRef: T, property: KProperty<*>): V? {
        val resolvedValue = value
        "value ${resolvedValue.toString()}<${typeToken.typeName}> successfully returned for ${property.name}".output(Colour.Green)
        return resolvedValue
    }
}

class HoconListProperty<T: HoconResolvable<T>, V: Any>(
    receiver: T,
    override val hoconEntry: HoconListEntry <T, V>,
    val hoconPrimitive:  HoconPrimitives<V>,
): HoconDelegateBase<T, V, List<V>>(receiver, hoconPrimitive.typeToken){

    override val componentName: String get() =  "HoconNullableProperty<T, List<${typeToken.typeName}>>[name: ${nameToUse}]"

    override fun getValue(thisRef: T, property: KProperty<*>): List<V> {
        val resolvedValue =  hoconEntry.listValue.getOrThrow {
            val msg = "value accessed before initialization. $componentName"
            managedException(msg)
        }
        "value ${resolvedValue}<${typeToken.typeName}> successfully returned for ${property.name}".output(Colour.Green)
        return resolvedValue
    }
}

inline fun <reified T: HoconResolvable<T>, reified V: Any>  T.hoconListProperty():HoconListProperty<T, V> {
    val primitive = PrimitiveClass.lookupPrimitive<V>()
    val genericList : HoconGenericList<V> = HoconGenericList(
        primitive,
        TypeToken.create<V>()
    )
    val entry = HoconListEntry<T, V>(this, genericList)
    val prop = HoconListProperty<T, V>(this, entry, genericList)
    return prop
}


class HoconNestedProperty<T: HoconResolvable<T>, V: HoconResolvable<V>>(
    receiver: T,
    override val hoconEntry: HoconNestedEntry<T, V>,
    hoconPrimitive:  HoconPrimitives<Any>,
    val nestedClass: V,
    typeToken: TypeToken<V>,
): HoconDelegateBase<T, Any, V>(receiver, hoconPrimitive.typeToken) {
    override val componentName: String get() = "HoconNestedProperty<T, ${typeToken.typeName}>[name: ${nameToUse}]"
    val  mandatory: Boolean = true
    override fun getValue(thisRef:T, property: KProperty<*>): V {
        return nestedClass
    }
}

inline fun <T: HoconResolvable<T>, reified V: HoconResolvable<V>> T.hoconNestedProperty(
    nestedClass: V,
):HoconNestedProperty<T, V> {
    resolver.registerMember(nestedClass)
    val entry = HoconNestedEntry(this, HoconObject, nestedClass)
    val prop = HoconNestedProperty<T, V>(this,entry,  HoconObject, nestedClass, TypeToken.create<V>())
    return prop
}

inline fun <T: HoconResolvable<T>, reified V: Any>  T.hoconProperty(
    hoconPrimitive:  HoconPrimitives<V>,
    mandatory: Boolean = true
):HoconProperty<T, V> {

    val entry =  HoconEntry<T, V>(this, hoconPrimitive, mandatory)
    val prop = HoconProperty<T, V>(this,entry,  hoconPrimitive)
    return prop
}

inline fun <T: HoconResolvable<T>, reified V: Any, R: Any>  T.hoconTransforming(
    hoconPrimitive:  HoconPrimitives<V>,
    noinline transformLambda: (V)->R
): HoconTransformProperty<T, V, R> {
    val entry =  HoconEntry<T, V>(this, hoconPrimitive, true)
    val prop = HoconTransformProperty<T, V, R>(this, entry, hoconPrimitive, transformLambda)
    return prop
}

inline fun <T: HoconResolvable<T>, reified V: Any>  T.hoconProperty(
    nullable: HoconNullable,
    hoconPrimitive:  HoconPrimitives<V>,
):HoconNullableProperty<T, V> {
    val entry = HoconNullableEntry<T, V>(this, hoconPrimitive)
    val prop = HoconNullableProperty<T, V>(this, entry,  hoconPrimitive)
    return prop
}


