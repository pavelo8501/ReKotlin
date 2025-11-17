package po.misc.debugging

import po.misc.context.CTX
import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.tracable.TraceableContext
import po.misc.debugging.models.ClassInfo
import po.misc.exceptions.stack_trace.extractTrace
import po.misc.functions.Nullable
import po.misc.types.helpers.qualifiedOrAnon
import po.misc.types.helpers.simpleOrAnon
import kotlin.reflect.KClass


interface ClassResolver {

    companion object{

        fun instanceName(receiver: Any): String {
            return when (receiver) {
                is Component -> {
                    val nullableComponentID: ComponentID? =  receiver.componentID as ComponentID?
                    nullableComponentID?.componentName ?:run {
                        receiver::class.simpleOrAnon
                    }
                }
                is CTX -> receiver.identifiedByName
                else -> receiver::class.simpleOrAnon
            }
        }

        fun classInfo(receiver: Any?): ClassInfo {
            return if(receiver != null){
                @Suppress("UNCHECKED_CAST")
                   when(val kClass = receiver::class){
                    is  Function<*> -> classInfo(kClass as KClass<out Function<*>>, receiver.hashCode(), "")
                    else -> classInfo(kClass, receiver.hashCode(), "")
                }

            }else{
                ClassInfo(Nullable)
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun classInfo(receiver: TraceableContext, resolveTrace: Boolean): ClassInfo{
            val instanceName =  instanceName(receiver)
           val classInfo = when(val kClass = receiver::class){
                is  Function<*> -> classInfo(kClass as KClass<out Function<*>>, receiver.hashCode(), instanceName)
                else -> classInfo(kClass, receiver.hashCode(), instanceName)
            }
            if(resolveTrace){
                val bestPickMeta = receiver.extractTrace().bestPick
                classInfo.addTraceInfo(bestPickMeta)
            }
            return classInfo
        }

        fun classInfo(kClass: KClass<out Function<*>>, hashCode: Int? = null): ClassInfo{
            val isSuspended = kClass.supertypes.any { it.toString().contains("Continuation") }
            val hasReceiver = when {
                kClass is Function2<*, *, *> && !isSuspended -> true
                else -> false
            }
            return ClassInfo(kClass, isSuspended, hasReceiver, hashCode)
        }

        fun  classInfo(kClass: KClass<*>, hashCode: Int = kClass.hashCode(), instanceName: String): ClassInfo {
            return ClassInfo(kClass, hashCode, instanceName)
        }

    }
}
