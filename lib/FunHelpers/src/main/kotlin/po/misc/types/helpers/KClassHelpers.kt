package po.misc.types.helpers

import kotlin.reflect.KClass


data class KClassParam(
    val simpleName : String,
    val qualifiedName: String,
    val hashCode: Int,
    val typeParameters: Int
){
    override fun toString(): String {
        return buildString {
            appendLine("Simple Name: $simpleName")
            appendLine("Qualified Name: $qualifiedName")
            appendLine("Hash Code: $hashCode")
            appendLine("Type Parameters Count: $typeParameters")
        }
    }
}


fun KClass<*>.toKeyParams():KClassParam{
    return KClassParam(simpleOrAnon, qualifiedName?:"N/A", hashCode(), typeParameters.size)
}