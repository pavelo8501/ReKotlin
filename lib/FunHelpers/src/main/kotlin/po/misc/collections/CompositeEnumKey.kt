package po.misc.collections

import po.misc.interfaces.Identifiable

class CompositeEnumKey<SO : Identifiable, E: Enum<E>>(
    private val sourceObject: SO,
    private val parameter: E,
): Comparable<CompositeEnumKey<SO, E>> {

    fun getEnumParameter():E{
        return parameter
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CompositeEnumKey<*, *>) return false
        return sourceObject.completeName == other.sourceObject.completeName &&
                parameter == other.parameter
    }
    override fun hashCode(): Int {
        return 31 * sourceObject.completeName.hashCode() + parameter.hashCode()
    }
    override fun compareTo(other: CompositeEnumKey<SO, E>): Int {
        val nameComparison = sourceObject.completeName.compareTo(other.sourceObject.completeName)
        return if (nameComparison != 0) nameComparison else parameter.compareTo(other.parameter)
    }
    override fun toString(): String = "CompositeKey(${sourceObject.completeName}, $parameter)"
}