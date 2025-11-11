package po.misc.debugging.models

import po.misc.data.PrettyPrint
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.types.helpers.simpleOrAnon
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
    val fromInstance: Boolean,
    val hashCode: Int,
    internal val kClass: KClass<*>,
): PrettyPrint{

    var simpleName : String = kClass.simpleOrAnon
    var qualifiedName: String = kClass.qualifiedName?:"Null"


    constructor(fromInstance: Boolean): this(fromInstance,  0, Unit::class){
        simpleName = "null"
        qualifiedName = "Null"
    }

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

    override fun toString(): String {
        return buildString {
            appendLine("Qualified: $qualifiedName")
            appendLine("Hash Code: $hashCode")
        }
    }
}
