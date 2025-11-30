package po.misc.debugging

import po.misc.context.CTX
import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.tracable.TraceableContext
import po.misc.debugging.models.ClassInfo
import po.misc.debugging.models.InstanceInfo
import po.misc.exceptions.trace
import po.misc.types.k_class.simpleOrAnon
import kotlin.reflect.KClass


interface ClassResolver {
    companion object {

        fun instanceName(receiver: Any): String {
            return when (receiver) {
                is Component -> {
                    val nullableComponentID: ComponentID? =  receiver.componentID as ComponentID?
                    nullableComponentID?.componentName ?:run {
                        "${receiver::class.simpleOrAnon} #${receiver.hashCode()}"
                    }
                }
                is CTX -> receiver.identifiedByName
                else -> "${receiver::class.simpleOrAnon} #${receiver.hashCode()}"
            }
        }

        fun instanceInfo(receiver: Any): InstanceInfo {
            val name = instanceName(receiver)
            val classInfo = classInfo(receiver)
           return  InstanceInfo(name,receiver.hashCode(),  classInfo)
        }

        fun classInfo(receiver: Any): ClassInfo {
            @Suppress("UNCHECKED_CAST")
              return when(val kClass = receiver::class){
                is  Function<*> -> classInfo(kClass as KClass<out Function<*>>)
                else -> classInfo(kClass)
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun classInfo(receiver: TraceableContext, resolveTrace: Boolean): ClassInfo{
           val classInfo = when(val kClass = receiver::class){
                is  Function<*> -> classInfo(kClass as KClass<out Function<*>>)
                else -> classInfo(kClass)
            }
            if(resolveTrace){
                val bestPickMeta = receiver.trace().bestPick
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
            return ClassInfo(kClass, isSuspended, hasReceiver)
        }

        fun  classInfo(kClass: KClass<*>): ClassInfo {
            return ClassInfo(kClass)
        }

        fun resolveInstance(context: TraceableContext): InstanceInfo{
            val name = instanceName(context)
            val classInfo = classInfo(context)
            return InstanceInfo(name, context.hashCode(), classInfo)
        }
    }
}


