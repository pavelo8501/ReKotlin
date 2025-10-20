package po.misc.debugging.models

import po.misc.context.component.ComponentID
import po.misc.data.PrettyPrint
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.types.token.TypeToken


data class GenericInfo(
    val name: String,
    val classInfo: ClassInfo
): PrettyPrint{

    override val formattedString: String = "${name.colorize(Colour.GreenBright)}: ${classInfo.simpleName.colorize(Colour.Yellow)}"
}

data class ClassInfo(
    val fromInstance: Boolean,
    val simpleName : String,
    val qualifiedName: String,
    val hashCode: Int
): PrettyPrint{

    internal val genericInfoBacking = mutableListOf<GenericInfo>()
    val genericInfo: List<GenericInfo> = genericInfoBacking

    private val genericParamsStr: String get() = genericInfo.joinToString(separator = ", ") {
        it.formattedString
    }

    override val formattedString: String get() = "${simpleName.colorize(Colour.Yellow)}<$genericParamsStr>"
    val isLambda: Boolean get() = qualifiedName.lowercase().contains("lambda")

    fun addParamInfo(parameterName: String,  typeToken: TypeToken<*>): ClassInfo{
        genericInfoBacking.add(GenericInfo(parameterName, ClassResolver.classInfo(typeToken.kClass)))
        return this
    }
}
