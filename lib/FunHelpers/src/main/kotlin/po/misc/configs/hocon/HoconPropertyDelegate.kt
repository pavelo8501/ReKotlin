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

sealed class HoconDelegateBase<T: HoconResolvable<T>, V>(
   private val resolvable:  T,
): Component {

    override var verbosity: Verbosity = Verbosity.Info
    var resolvedProperty: KProperty<*>? = null
        protected set


    protected val resolver : HoconConfigResolver<T> get() = resolvable.resolver
    abstract val hoconEntry: HoconEntryBase<T, *>
    var value: V? = null
        protected set

    protected val propertyBacking: KProperty<*> get() = resolvedProperty.getOrThrow(this){
        it.methodName = "propertyBacking"
        managedException(it.message)
    }

    protected val nameToUse: String get() {
        return  resolvedProperty?.name?: "Unresolved"
    }


    fun resolveProperty(property: KProperty<*>){
        resolvedProperty = property
    }

}

class HoconProperty<T: HoconResolvable<T>, V: Any>(
    resolvable:  T,
    val hoconPrimitive:  HoconPrimitives<V>,
    mandatory: Boolean,
): HoconDelegateBase<T,  V>(resolvable),  ReadOnlyProperty<T, V>{


    val typeToken :TypeToken<V> get() = hoconPrimitive.typeToken

    override val componentName: String get() =  "HoconProperty<T, ${typeToken.typeName}>[${nameToUse}]"
    var  mandatory: Boolean = true



    override val hoconEntry: HoconEntry<T, V>  by lazy {
        HoconEntry(resolvable.resolver, propertyBacking, hoconPrimitive, mandatory)
    }

    override fun getValue(thisRef: T, property: KProperty<*>): V {

       val resolvedValue = value.getOrThrow {
            val msg = "value accessed before initialization. $componentName"
            managedException(msg)
        }
        "value ${resolvedValue.toString()}<${typeToken.typeName}> successfully returned for ${property.name}".output(Colour.Green)
        return resolvedValue
    }
    operator fun provideDelegate(thisRef: T, property: KProperty<*>): HoconProperty<T, V> {
        resolveProperty(property)
        hoconEntry.valueAvailable {
            value = it
        }
        return this
    }
}

class HoconNullableProperty<T: HoconResolvable<T>, V: Any>(
    resolvable:  T,
   val  hoconPrimitive:  HoconPrimitives<V>,
): HoconDelegateBase<T,  V>(resolvable),  ReadOnlyProperty<T, V?>  {

    val typeToken :TypeToken<V> get() = hoconPrimitive.typeToken

    override val componentName: String get() =  "HoconNullableProperty<T, ${typeToken.typeName}>[name: ${nameToUse}]"
    val nullable: HoconNullable = HoconNullable

    override val hoconEntry: HoconNullableEntry<T, V>  by lazy {
        HoconNullableEntry(resolvable.resolver, propertyBacking, hoconPrimitive)
    }

    override fun getValue(thisRef: T, property: KProperty<*>): V? {
        val resolvedValue = value
        "value ${resolvedValue.toString()}<${typeToken.typeName}> successfully returned for ${property.name}".output(Colour.Green)
        return resolvedValue
    }

    operator fun provideDelegate(thisRef: T, property: KProperty<*>): HoconNullableProperty<T, V> {
        resolveProperty(property)
        hoconEntry.valueAvailable {providedValue->
            value = providedValue
        }
        return this
    }
}

class HoconListProperty<T: HoconResolvable<T>, V: Any>(
    resolvable: T,
   val hoconPrimitive:  HoconPrimitives<V>,
): HoconDelegateBase<T, List<V>>(resolvable),  ReadOnlyProperty<T, List<V>>  {

    val typeToken :TypeToken<V> get() = hoconPrimitive.typeToken

    override val componentName: String get() =  "HoconNullableProperty<T, List<${typeToken.typeName}>>[name: ${nameToUse}]"

    override val hoconEntry: HoconListEntry <T, V>  by lazy {
        HoconListEntry(resolvable.resolver, propertyBacking, hoconPrimitive)
    }

    override fun getValue(thisRef: T, property: KProperty<*>): List<V> {
        val resolvedValue =  hoconEntry.listValue.getOrThrow {
            val msg = "value accessed before initialization. $componentName"
            managedException(msg)
        }
        "value ${resolvedValue.toString()}<${typeToken.typeName}> successfully returned for ${property.name}".output(Colour.Green)
        return resolvedValue
    }
    operator fun provideDelegate(thisRef: T, property: KProperty<*>): HoconListProperty<T, V> {
        resolveProperty(property)
        hoconEntry
        return this
    }
}


inline fun <reified T: HoconResolvable<T>, reified V: Any>  T.hoconListProperty(

):HoconListProperty<T, V> {

    val primitive = PrimitiveClass.lookupPrimitive<V>()
    val genericList : HoconGenericList<V> = HoconGenericList(
        primitive,
        TypeToken.create<V>()
    )
    val prop = HoconListProperty<T, V>(this, genericList)
    return prop
}



class HoconNestedProperty<T: HoconResolvable<T>, V: HoconResolvable<V>>(
    resolvable: T,
    hoconPrimitive:  HoconPrimitives<Any>,
    val nestedClass: V,
    val typeToken: TypeToken<V>,
): HoconDelegateBase<T,  V>(resolvable), ReadOnlyProperty<T, V> {

    override val componentName: String get() = "HoconNestedProperty<T, ${typeToken.typeName}>[name: ${nameToUse}]"
    val  mandatory: Boolean = true

    override val hoconEntry: HoconNestedEntry<T, V> by lazy {
        HoconNestedEntry(resolvable.resolver, propertyBacking, hoconPrimitive, nestedClass)
    }

    override fun getValue(thisRef:T, property: KProperty<*>): V {
        return nestedClass
    }
    operator fun provideDelegate(thisRef: T, property: KProperty<*>): HoconNestedProperty<T, V> {
        resolveProperty(property)
        //Access to lazy property just to start initialization
        hoconEntry
        return this
    }
}

inline fun <T: HoconResolvable<T>, reified V: HoconResolvable<V>>  T.hoconNestedProperty(
    nestedClass: V,
):HoconNestedProperty<T, V> {

    resolver.registerMember(nestedClass)
    val prop = HoconNestedProperty<T, V>(this, HoconObject, nestedClass, TypeToken.create<V>())
    return prop
}

inline fun <T: HoconResolvable<T>, reified V: Any>  T.hoconProperty(
    hoconPrimitive:  HoconPrimitives<V>,
    mandatory: Boolean = true
):HoconProperty<T, V> {
    val prop = HoconProperty<T, V>(this, hoconPrimitive, mandatory)
    prop.mandatory = mandatory
    return prop
}

inline fun <T: HoconResolvable<T>, reified V: Any>  T.hoconProperty(
    nullable: HoconNullable,
    hoconPrimitive:  HoconPrimitives<V>,
):HoconNullableProperty<T, V> {
    val prop = HoconNullableProperty<T, V>(this, hoconPrimitive)
    return prop
}


