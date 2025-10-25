package po.misc.debugging.models

import po.misc.data.PrettyPrint
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass
import kotlin.reflect.KType


data class GenericInfo(
    val parameterName: String,
    val kType: KType,
    val classInfo: ClassInfo
): PrettyPrint{


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

data class ClassInfo(
    internal val kClass: KClass<*>,
    val fromInstance: Boolean,
    val simpleName : String,
    val qualifiedName: String,
    val hashCode: Int,
): PrettyPrint{

    internal val genericInfoBacking = mutableListOf<GenericInfo>()

    val genericInfo: List<GenericInfo> = genericInfoBacking

    private val genericParamsStr: String get() = genericInfo.joinToString(separator = ", ") {
        it.formattedString
    }

    override val formattedString: String get() = "${simpleName.colorize(Colour.Yellow)}<$genericParamsStr>"
    val isLambda: Boolean get() = qualifiedName.lowercase().contains("lambda")

    fun addParamInfo(parameterName: String,  typeToken: TypeToken<*>): GenericInfo{
       val info = GenericInfo(parameterName, typeToken.kType, ClassResolver.classInfo(typeToken.kClass))
        genericInfoBacking.add(info)
        return info
    }
}
