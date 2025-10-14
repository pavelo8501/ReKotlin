package po.misc.types.token

import po.misc.types.helpers.simpleNameOr
import po.misc.types.helpers.simpleOrNan
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter


class TypeSlot(val parameter:KTypeParameter){
    var ownReifiedKType : KType? = null
    val name : String  = parameter.name
    val isReified : Boolean  get() {
        return if(ownReifiedKType != null){
            true
        }else{
            parameter.isReified
        }
    }
    val upperBoundsClass :  List<KClass<*>> = parameter.upperBounds.mapNotNull { it.classifier  as? KClass<*> }

    var ownClass: KClass<*>? = null
    fun addResolvedParameterClass(resolved: KClass<*>):TypeSlot{
        ownClass = resolved
        return this
    }
    override fun toString(): String {
        return buildString {
            appendLine("Type Parameter: $name")
            appendLine("Class :" + ownClass.simpleNameOr("Not Available"))
            appendLine("IsReified: $isReified")
            appendLine("Upper bounds: " + (upperBoundsClass.lastOrNull()?.simpleOrNan()?:"Not Available"))
        }
    }
}
