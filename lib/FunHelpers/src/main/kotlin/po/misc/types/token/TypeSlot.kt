package po.misc.types.token


import po.misc.data.PrettyPrint
import po.misc.debugging.models.GenericInfo
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter


class TypeSlot(
    val genericInfo: GenericInfo,
    private val parameter: KTypeParameter
): PrettyPrint{

    val isMarkedNullable: Boolean get() = genericInfo.isMarkedNullable
    val parameterName: String = genericInfo.parameterName
    val upperBoundsClass : List<KClass<*>> = parameter.upperBounds.mapNotNull { it.classifier  as? KClass<*> }
    val kClass: KClass<*> = genericInfo.classInfo.kClass
    val kType: KType = genericInfo.kType

    override val formattedString: String = genericInfo.formattedString

    fun toCompareContainer():CompereContainer{
       return CompereContainer(kClass, isMarkedNullable)
    }

    override fun toString(): String {
       return genericInfo.toString()
    }
}


//var ownReifiedKType : KType? = null
//val name : String  = parameter.name
//val isReified : Boolean  get() {
//    return if(ownReifiedKType != null){
//        true
//    }else{
//        parameter.isReified
//    }
//}
//val upperBoundsClass :  List<KClass<*>> = parameter.upperBounds.mapNotNull { it.classifier  as? KClass<*> }
//var ownClass: KClass<*>? = null
//
//fun addResolvedParameterClass(resolved: KClass<*>):TypeSlot{
//    ownClass = resolved
//    return this
//}
//
//override fun toString(): String {
//    return buildString {
//        appendLine("Type Parameter: $name")
//        appendLine("Class :" + ownClass.simpleNameOr("Not Available"))
//        appendLine("IsReified: $isReified")
//        appendLine("Upper bounds: " + (upperBoundsClass.lastOrNull()?.simpleOrAnon?:"Not Available"))
//    }
//}
