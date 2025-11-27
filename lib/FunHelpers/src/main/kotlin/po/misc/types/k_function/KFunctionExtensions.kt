package po.misc.types.k_function

import po.misc.context.tracable.TraceableContext
import po.misc.debugging.ClassResolver
import po.misc.types.castOrThrow
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter


fun  KFunction<*>.receiverClass(): KClass<*>?{
    val classByInstance =   instanceParameter?.type?.classifier as?  KClass<*>
    if(classByInstance != null){
        return classByInstance
    }
    return extensionReceiverParameter?.type?.classifier as?  KClass<*>
}

val KFunction<TraceableContext>.receiverClass : KClass<out TraceableContext> get() {
    val kClass =  receiverClass()
        ?: throw IllegalStateException("TraceableContext should have had a classifier")
    return kClass.castOrThrow<KClass<out TraceableContext>>()
}

val KClass<out Function<*>>.lambdaName: String  get()  = ClassResolver.classInfo(this).normalizedName


