package po.misc.configs.hocon.properties

import po.misc.configs.hocon.models.HoconEntry
import po.misc.configs.hocon.models.HoconEntryBase
import po.misc.configs.hocon.models.HoconListEntry
import po.misc.configs.hocon.models.HoconNestedEntry
import po.misc.configs.hocon.models.HoconNullableEntry
import po.misc.configs.hocon.HoconResolvable
import po.misc.configs.hocon.models.HoconNullable
import po.misc.configs.hocon.models.HoconPrimitives
import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.data.helpers.output
import po.misc.data.logging.Verbosity
import po.misc.data.styles.Colour
import po.misc.exceptions.managedException
import po.misc.types.getOrThrow
import po.misc.types.token.TypeToken
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


sealed class HoconDelegateBase<T: HoconResolvable<T>, V: Any, R>(
   protected val receiver:  T,
   protected val typeToken: TypeToken<V>
): Component,  ReadOnlyProperty<T, R> {

    abstract var verbosity: Verbosity

    var resolvedProperty: KProperty<*>? = null
        protected set

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

class HoconProperty<T: HoconResolvable<T>, V: Any>(
    receiver:  T,
    override val hoconEntry: HoconEntry<T, V>,
    val hoconPrimitive: HoconPrimitives<V>,
): HoconDelegateBase<T, V, V>(receiver, hoconPrimitive.typeToken){


    override val componentID: ComponentID = componentID(nameToUse).addParamInfo("T", typeToken)
    override var verbosity: Verbosity
        get() = componentID.verbosity
        set(value) {
            componentID.verbosity = value
        }

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        val resolvedValue = value.getOrThrow {
            val msg = "value accessed before initialization. ${componentID.name}"
            managedException(msg)
        }
        "value ${resolvedValue.toString()}<${typeToken.typeName}> successfully returned for ${property.name}".output(Colour.Green)
        return resolvedValue
    }
}

class HoconTransformProperty<T: HoconResolvable<T>, V: Any, R: Any>(
    receiver:  T,
    override val hoconEntry: HoconEntry<T, V>,
    val hoconPrimitive: HoconPrimitives<V>,
    val transformLambda: (V)->R
): HoconDelegateBase<T, V, R>(receiver, hoconPrimitive.typeToken){


    override val componentID: ComponentID = componentID().addParamInfo("T", typeToken)
    override var verbosity: Verbosity
        get() = componentID.verbosity
        set(value) {
            componentID.verbosity = value
        }


    private val valueRequest: V  get() = value.getOrThrow {
        val msg = "value accessed before initialization. ${componentID.name}"
        managedException(msg)
    }
    private var resultBacking: R? = null
    val result: R  get() =  resultBacking?:transformLambda(valueRequest)

    override fun getValue(thisRef: T, property: KProperty<*>): R {
        val resolvedValue = value.getOrThrow {
            val msg = "value accessed before initialization. ${componentID.name}"
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

class HoconNullableProperty<T: HoconResolvable<T>, V: Any>(
    receiver: T,
    override val hoconEntry: HoconNullableEntry<T, V>,
    val  hoconPrimitive: HoconPrimitives<V>,
): HoconDelegateBase<T,  V, V?>(receiver, hoconPrimitive.typeToken) {


    override val componentID: ComponentID = componentID(nameToUse).addParamInfo("T", typeToken)
    val componentName: String get() =  componentID.name
    override var verbosity: Verbosity
        get() = componentID.verbosity
        set(value) { componentID.verbosity = value }

    val nullable: HoconNullable = HoconNullable

    override fun getValue(thisRef: T, property: KProperty<*>): V? {
        val resolvedValue = value
        "value ${resolvedValue.toString()}<${typeToken.typeName}> successfully returned for ${property.name}".output(Colour.Green)
        return resolvedValue
    }
}

class HoconListProperty<T: HoconResolvable<T>, V: Any>(
    receiver: T,
    override val hoconEntry: HoconListEntry<T, V>,
    val hoconPrimitive: HoconPrimitives<V>,
): HoconDelegateBase<T, V, List<V>>(receiver, hoconPrimitive.typeToken){


    override val componentID: ComponentID = componentID(nameToUse).addParamInfo("T", typeToken)
    val componentName: String get() =  componentID.name
    override var verbosity: Verbosity
        get() = componentID.verbosity
        set(value) { componentID.verbosity = value }

    override fun getValue(thisRef: T, property: KProperty<*>): List<V> {
        val resolvedValue =  hoconEntry.listValue.getOrThrow {
            val msg = "value accessed before initialization. $componentName"
            managedException(msg)
        }
        "value ${resolvedValue}<${typeToken.typeName}> successfully returned for ${property.name}".output(Colour.Green)
        return resolvedValue
    }
}

class HoconNestedProperty<T: HoconResolvable<T>, V: HoconResolvable<V>>(
    receiver: T,
    override val hoconEntry: HoconNestedEntry<T, V>,
    hoconPrimitive: HoconPrimitives<Any>,
    val nestedClass: V,
    typeToken: TypeToken<V>,
): HoconDelegateBase<T, Any, V>(receiver, hoconPrimitive.typeToken) {

    override val componentID: ComponentID = componentID(nameToUse).addParamInfo("T", typeToken)
    val componentName: String get() =  componentID.name
    override var verbosity: Verbosity
        get() = componentID.verbosity
        set(value) { componentID.verbosity = value }

    override fun getValue(thisRef:T, property: KProperty<*>): V {
        return nestedClass
    }
}

