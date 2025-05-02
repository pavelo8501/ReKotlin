package po.misc.collections


interface Identifiable{
    val qualifiedName: String
}


class CompositeKey<SO : Identifiable, E: Enum<E>>(
    private val sourceObject: SO,
    private val parameter: E,
): Comparable<CompositeKey<SO, E>> {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CompositeKey<*, *>) return false
        return sourceObject.qualifiedName == other.sourceObject.qualifiedName &&
                parameter == other.parameter
    }
    override fun hashCode(): Int {
        return 31 * sourceObject.qualifiedName.hashCode() + parameter.hashCode()
    }
    override fun compareTo(other: CompositeKey<SO, E>): Int {
        val nameComparison = sourceObject.qualifiedName.compareTo(other.sourceObject.qualifiedName)
        return if (nameComparison != 0) nameComparison else parameter.compareTo(other.parameter)
    }
    override fun toString(): String = "CompositeKey(${sourceObject.qualifiedName}, $parameter)"
}