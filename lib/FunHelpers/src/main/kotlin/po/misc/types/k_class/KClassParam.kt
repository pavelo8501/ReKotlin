package po.misc.types.k_class

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