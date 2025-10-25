package po.misc.debugging

import po.misc.context.CTX
import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.tracable.TraceableContext
import po.misc.debugging.models.ClassInfo
import po.misc.types.helpers.qualifiedOrAnon
import po.misc.types.helpers.simpleOrAnon
import kotlin.reflect.KClass


interface ClassResolver {

    companion object{

        fun instanceName(receiver: TraceableContext): String {
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

        fun classInfo(receiver: Any): ClassInfo {
            val kClass = receiver::class
            return ClassInfo(
                kClass,
                true,
                kClass.simpleOrAnon,
                kClass.qualifiedOrAnon,
                hashCode()
            )
        }

        fun  classInfo(kClass: KClass<*>): ClassInfo {
            return ClassInfo(
                kClass,
                false,
                kClass.simpleOrAnon,
                kClass.qualifiedOrAnon,
                hashCode(),
            )
        }
    }
}
