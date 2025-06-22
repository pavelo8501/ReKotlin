package po.misc.collections

import po.misc.interfaces.Identifiable

class CompositeEnumKey<E: Enum<E>>(
    private val enumKey: E,
    private val source: Identifiable,
): Comparable<CompositeEnumKey<E>> {

    fun getEnumParameter():E{
        return enumKey
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CompositeEnumKey<E>) return false
        return source.completeName == other.source.completeName &&
                enumKey == other.enumKey
    }
    override fun hashCode(): Int {
        return 31 * source.completeName.hashCode() + enumKey.hashCode()
    }
    override fun compareTo(other: CompositeEnumKey<E>): Int {
        val nameComparison = source.completeName.compareTo(other.source.completeName)
        return if (nameComparison != 0) nameComparison else enumKey.compareTo(other.enumKey)
    }
    override fun toString(): String = "CompositeKey(${source.completeName}, $enumKey)"

    companion object{
        fun <E: Enum<E>> generateKey(key:E, source: Identifiable):CompositeEnumKey<E>{
            return CompositeEnumKey(key, source)
        }

        fun <E: Enum<E>> Identifiable.generateKey(key:E):CompositeEnumKey<E>{
            return CompositeEnumKey(key, this)
        }
    }
}