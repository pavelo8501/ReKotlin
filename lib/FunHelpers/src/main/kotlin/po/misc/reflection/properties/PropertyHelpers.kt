package po.misc.reflection.properties

import po.misc.data.output.output
import po.misc.data.styles.Colour
import po.misc.exceptions.throwableToText
import po.misc.reflection.mappers.models.PropertyRecord
import po.misc.types.castOrManaged
import po.misc.types.safeCast
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure


inline fun <reified T: Any> toPropertyMap(): Map<String, PropertyRecord<T>>
    = T::class.memberProperties.associate {it.name to PropertyRecord.create(it)}




fun <T: Any> toPropertyMap(clazz: KClass<T>):Map<String, PropertyInfo<T, Any>> {
    val propertyInfoList = clazz.memberProperties.mapNotNull { it.safeCast<KProperty1<T, Any>>()?.toPropertyInfo(clazz)}
    return  propertyInfoList.associateBy{ it.propertyName }
}


fun <T,  P> KProperty1<T, P>.testMutability(): KMutableProperty1<T, P>?{
    return  safeCast<KMutableProperty1<T, P>>()
}


@PublishedApi
internal inline fun <T: Any, reified P : Any> propertyLookup(
    receiverClass: KClass<T>,
    lookUpClass: KClass<P>,
): List<KProperty1<T, P>> {
  return receiverClass::class.memberProperties.filter { filteredProp ->
       filteredProp.returnType.jvmErasure == lookUpClass }.mapNotNull { mapped ->
       mapped.safeCast<KProperty1<T, P>>()
   }
}

inline fun <reified T: Any, reified P : Any> mutablePropertyLookup(
): List<KMutableProperty1<T, P>> {

    return T::class.memberProperties.filter { filteredProp ->
        filteredProp.returnType.jvmErasure == P::class }.mapNotNull { mapped ->
        mapped.safeCast<KMutableProperty1<T, P>>()
    }
}

inline fun <T: Any, reified P : Any>  T.mutablePropertyLookup(
    kClass: KClass<P>
): List<KMutableProperty1<T, P>> {

    return this::class.memberProperties.filter { filteredProp ->
        filteredProp.returnType.jvmErasure == P::class }.mapNotNull { mapped ->
        mapped.safeCast<KMutableProperty1<T, P>>()
    }
}


inline fun <T: Any, reified P : Any>  T.findMutableOfType(kClass: KClass<P>):List<KMutableProperty1<T, P>> {
    val thisClass = this::class.castOrManaged<KClass<T>>(this)
    return mutablePropertyLookup(kClass)
}

inline fun <reified T: Any, reified P : Any> findMutableOfType(): List<KMutableProperty1<T, P>> = mutablePropertyLookup<T, P>()


inline fun <T: Any, reified P : Any>  T.findPropertiesOfType(kClass: KClass<P>): List<KProperty1<T, P>> {
    val thisClass = this::class.castOrManaged<KClass<T>>(this)
    return propertyLookup(thisClass, kClass)
}
inline fun <reified T: Any, reified P : Any>  findPropertiesOfType(): List<KProperty1<T, P>> = propertyLookup<T, P>(T::class, P::class)



fun KType.convertValue(input: Any): Any {

    return when(val classifier = this.classifier) {
         input::class -> input
         Int::class -> input.toString().toInt()
         String::class -> input.toString()
         Boolean::class -> input.toString().toBooleanStrictOrNull() ?: false
         Long::class -> input.toString().toLong()
        LocalDateTime::class -> LocalDateTime.parse(input.toString())

        is KClass<*> if classifier.java.isEnum -> {
            @Suppress("UNCHECKED_CAST")
            java.lang.Enum.valueOf(classifier.java as Class<out Enum<*>>, input.toString())
        }
        else -> throw IllegalArgumentException("Cannot convert $input to $classifier")
    }
}


fun <T: Any> KProperty1<T, *>.convertFromString(input: String): Any {
    val result = when (val classifier = returnType.classifier) {
        Int::class -> input.toInt()
        String::class -> input
        Boolean::class -> input.toBooleanStrictOrNull() ?: false
        Long::class -> input.toLong()
        LocalDateTime::class -> {
            LocalDate.parse(input)
        }
        is KClass<*> -> {
            if (classifier.java.isEnum) {
                @Suppress("UNCHECKED_CAST")
                java.lang.Enum.valueOf(classifier.java as Class<out Enum<*>>, input)
            } else {
                throw IllegalArgumentException("Unsupported type: $classifier")
            }
        }
        else -> throw IllegalArgumentException("Unknown type: $classifier for input$input")
    }
    return result
}


fun <T: Any> KMutableProperty1<T, Any>.updateConverting(receiver: T, value: Any): Boolean {
    return try {
        val converted = this.returnType.convertValue(value)
        this.set(receiver, converted)
        true
    } catch (th: Throwable) {
        th.throwableToText().output(Colour.RedBright)
        false
    }
}


