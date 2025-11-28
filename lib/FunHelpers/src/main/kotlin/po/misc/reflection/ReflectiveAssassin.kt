package po.misc.reflection

import po.misc.types.castOrThrow
import po.misc.types.k_class.simpleOrAnon
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter


interface ReflectiveAssassin{

    companion object{

        internal fun brutForcePropertyName(
            propName: String,
            lookupResult: PropertyLookup,
            javaClass: Class<*>,
            propDelegate: Boolean = false
        ):  Field? {
            return try {

                val delegateField =  if(propDelegate){
                    javaClass.getDeclaredField("${propName}\$delegate")
                }else{
                    javaClass.declaredFields.firstOrNull {
                        it.name == propName|| it.name.contains(propName)
                    }
                }
                if(delegateField!= null){
                    delegateField.isAccessible = true
                    delegateField
                }else{
                    null
                }
            }catch (ex: Throwable){
                lookupResult.registerThrowable(ex)
                null
            }
        }

        internal inline fun <reified T: Any> bypassConstructorVisibility():T?{
            val clazz = T::class
            val lookupResult = PropertyLookup(clazz)
           return try {
                val constructor = clazz.java.declaredConstructors.first().apply { isAccessible = true }
                 constructor.newInstance()?.let {
                     lookupResult.registerData(it.toString(), clazz.simpleOrAnon)
                     it.castOrThrow<T>()
                }
            }catch (ex: Throwable){
                lookupResult.registerThrowable(ex)
                null
            }
        }

        @PublishedApi
        internal fun playDirty(
            receiver: Any,
            prop: KProperty<*>,
            lookup: PropertyLookup
        ): Any? {
            val kClass = receiver::class
            val jClass = kClass.java

            val candidates = buildList<AccessibleObject> {
                jClass.declaredFields.forEach { if (it.name == prop.name || it.name.contains(prop.name)) add(it) }
                prop.javaGetter?.let { add(it) }
                prop.javaField?.let { add(it) }
            }
            for (candidate in candidates) {
                try {
                    candidate.isAccessible = true
                    val value = when (candidate) {
                        is Field -> candidate.get(receiver)
                        is Method -> candidate.invoke(receiver)
                        else -> continue
                    }
                    lookup.registerData(value, prop)
                    return value
                } catch (t: Throwable) {
                    lookup.registerThrowable(t)
                }
            }
            lookup.registerThrowable(IllegalStateException("No reflective access for ${prop.name}"))
            return null
        }
    }

}