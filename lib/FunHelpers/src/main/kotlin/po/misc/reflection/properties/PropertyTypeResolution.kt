package po.misc.reflection.properties

import po.misc.data.output.output
import po.misc.data.styles.Colour
import po.misc.reflection.primitives.BooleanClass
import po.misc.reflection.primitives.DoubleClass
import po.misc.reflection.primitives.DurationClass
import po.misc.reflection.primitives.IntClass
import po.misc.reflection.primitives.LocalDateTimeClass
import po.misc.reflection.primitives.LongClass
import po.misc.reflection.primitives.PrimitiveClass
import po.misc.reflection.primitives.StringClass
import po.misc.types.castOrManaged
import po.misc.types.k_class.simpleOrAnon
import po.misc.types.safeCast
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties



@PublishedApi
internal fun <T: Any, V: Any> toTypedContainer(
    valueClass: KClass<V>,
    property: KMutableProperty1<T, *>,
): PropertySimpleTypeContainer<T>? {

      val result =   when (valueClass) {
            Int::class -> {
                val casted = property.castOrManaged<KMutableProperty1<T, Int>>(property)
                IntPropertyContainer<T>(casted)
            }
            String::class -> {
                val casted = property.castOrManaged<KMutableProperty1<T, String>>(property)
                StringPropertyContainer<T>(casted)
            }
            Boolean::class -> {
                val casted = property.castOrManaged<KMutableProperty1<T, Boolean>>(property)
                BoolPropertyContainer(casted)
            }
            Long::class -> {
                val casted = property.castOrManaged<KMutableProperty1<T, Long>>(property)
                LongPropertyContainer<T>(casted)
            }
            else -> {
                "Unsupported Type ${valueClass.simpleOrAnon}"
                null
            }
        }
    return result
}


inline fun <reified T: Any, reified V : Any> KProperty<*>.toTypedContainer(): PropertySimpleTypeContainer<T>? {
  return  safeCast<KMutableProperty1<T, *>>()?.let {mutableProperty->
        val valueClass = V::class
        when(valueClass){
            Int::class ->{
                val casted = mutableProperty.castOrManaged<KMutableProperty1<T, Int>>(this)
                IntPropertyContainer<T>(casted)
            }
            String::class -> {
                val casted = mutableProperty.castOrManaged<KMutableProperty1<T, String>>(this)
                StringPropertyContainer<T>(casted)
            }
            Boolean::class -> {
                val casted = mutableProperty.castOrManaged<KMutableProperty1<T, Boolean>>(this)
                BoolPropertyContainer(casted)
            }
            Long::class -> {
                val casted = mutableProperty.castOrManaged<KMutableProperty1<T, Long>>(this)
                LongPropertyContainer<T>(casted)
            }
            else -> {
                TODO("Unsupported type")
            }
        }
    }
}

fun <T: Any> KProperty1<T, *>.toTypedContainer(
    receiver:T,
    primitiveClass: PrimitiveClass<*>,
    onCreation: (PrimitiveClass<*>)-> Any
): PropertySimpleTypeContainer<T>? {

   return when (primitiveClass) {
        BooleanClass -> null
        DoubleClass -> null
        DurationClass -> null
        IntClass -> {
            safeCast<KMutableProperty1<T, Int>>()?.let { mutableProperty ->
               val container = IntPropertyContainer(mutableProperty)
                val withValue = onCreation.invoke(IntClass)
                container.updateValue(receiver, withValue.castOrManaged(this))
                container
            }?:run {
                ReadOnlyIntProperty(this.castOrManaged(this))
            }
        }
        LocalDateTimeClass -> null
        LongClass -> null
        StringClass -> {
            safeCast<KMutableProperty1<T, String>>()?.let { mutableProperty ->
                val container = StringPropertyContainer(mutableProperty)
                val withValue = onCreation.invoke(StringClass)
                container.updateValue(receiver, withValue.castOrManaged(this))
                container
            }?:run {
                ReadOnlyStringProperty(this.castOrManaged(this))
            }
        }
        else ->{
            TODO("$primitiveClass not supported yet")
        }
    }
}

fun <T: Any> KProperty1<T, *>.toTypedContainer(receiver:T): PropertySimpleTypeContainer<T>? {

    val value = get(receiver)
    if(value == null){
        return null
    }else {
        return safeCast<KMutableProperty1<T, *>>()?.let { mutableProperty ->
            val valueClass = value::class
            when (valueClass) {
                Int::class -> {
                    val casted = mutableProperty.castOrManaged<KMutableProperty1<T, Int>>(this)
                    IntPropertyContainer<T>(casted)
                }
                String::class -> {
                    val casted = mutableProperty.castOrManaged<KMutableProperty1<T, String>>(this)
                    StringPropertyContainer<T>(casted)
                }
                Boolean::class -> {
                    val casted = mutableProperty.castOrManaged<KMutableProperty1<T, Boolean>>(this)
                    BoolPropertyContainer(casted)
                }
                Long::class -> {
                    val casted = mutableProperty.castOrManaged<KMutableProperty1<T, Long>>(this)
                    LongPropertyContainer<T>(casted)
                }
                else -> {
                    TODO("$mutableProperty not supported yet")
                }
            }
        }
    }
}

inline fun <reified T: Any> KClass<T>.createTypedProperties(): List<PropertySimpleTypeContainer<T>> {
    val basicClasses = listOf<KClass<*>>(Int::class, Long::class, Boolean::class, String::class)
    val result = mutableListOf<PropertySimpleTypeContainer<T>>()
    for (property in memberProperties) {
        property.safeCast<KMutableProperty1<T, *>>()?.let { mutable ->
            val returnClass = mutable.returnType.classifier as? KClass<*>
            returnClass?.let {
                basicClasses.firstOrNull { baseClass -> baseClass == it }?.let { resolvedClass ->
                    toTypedContainer(resolvedClass, mutable)?.let { container ->
                        result.add(container)
                    }
                }
            }
        }?:run {
            "Container for property ${property.name} not crated. Property is immutable".output(Colour.Yellow)
        }
    }
    return result
}