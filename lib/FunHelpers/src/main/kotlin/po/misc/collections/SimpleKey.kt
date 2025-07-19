package po.misc.collections

import po.misc.interfaces.ValueBased


class SimpleKey (
    val name: String,
    val type: ValueBased
):Comparable<SimpleKey> {


    val key: String
        get() = "CompositeKey(${name}:${type.value})"

    override fun toString(): String = key

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleKey) return false
        return key == other.key &&
                type.value == other.type.value
    }

    override fun hashCode(): Int {
        return 31 * key.hashCode()
    }

    override fun compareTo(other: SimpleKey): Int {
        val componentComparison = key.compareTo(other.key)
        return if (componentComparison != 0) componentComparison
        else key.compareTo(other.key)
    }
}

