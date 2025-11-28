package po.misc.types.k_function

import po.misc.debugging.ClassResolver
import po.misc.types.k_class.simpleOrAnon
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.javaMethod

fun KFunction<*>.receiverClass(): KClass<*>?{
    instanceParameter
        ?.type?.classifier
        ?.let { it as? KClass<*> }
        ?.let { return it }

    extensionReceiverParameter
        ?.type?.classifier
        ?.let { it as? KClass<*> }
        ?.let { return it }
    return javaMethod?.declaringClass?.kotlin
}

val KFunction<*>.receiverClasName: String get() {
    return receiverClass()?.simpleOrAnon?:"Unavailable"
}


val KClass<out Function<*>>.lambdaName: String  get()  = ClassResolver.classInfo(this).normalizedName