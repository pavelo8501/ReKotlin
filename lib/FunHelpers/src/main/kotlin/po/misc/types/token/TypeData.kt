package po.misc.types.token

import po.misc.data.PrettyPrint
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.debugging.models.ClassInfo
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter

data class GenericInfo(
    val parameterName: String,
    val kType: KType,
    val classInfo: ClassInfo
): PrettyPrint {
    private val classDisplayName: String  get() {
        return if(kType.isMarkedNullable){
            "${classInfo.simpleName}?".colorize(Colour.Yellow)
        }else{
            classInfo.simpleName.colorize(Colour.Yellow)
        }
    }
    val isMarkedNullable: Boolean get() = kType.isMarkedNullable
    override val formattedString: String = "${parameterName.colorize(Colour.GreenBright)}: $classDisplayName"
}

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

    fun toComparePair():Pair<KClass<*>, Boolean>{
        return Pair(kClass, isMarkedNullable)
    }


    override fun toString(): String {
        return genericInfo.toString()
    }
}