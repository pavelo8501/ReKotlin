package po.misc.types

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

@JvmName("findPropertiesOfTypeStrictTyped")
inline fun <T: Any, reified P : Any> KClass<T>.findPropertiesOfType(): List<KProperty1<T, P>> {
    @Suppress("UNCHECKED_CAST")
    return this.memberProperties
        .filter { filteredProp->
            filteredProp.returnType.jvmErasure == P::class
        }
        .mapNotNull {mapped->
            mapped  as? KProperty1<T, P>
        }
}


inline fun <reified P : Any> KClass<*>.findPropertiesOfType(): List<KProperty1<Any, P>> {
    @Suppress("UNCHECKED_CAST")
    return this.memberProperties
        .filter { filteredProp->
            filteredProp.returnType.jvmErasure == P::class
        }
        .mapNotNull {mapped->
            mapped  as? KProperty1<Any, P>
        }
}