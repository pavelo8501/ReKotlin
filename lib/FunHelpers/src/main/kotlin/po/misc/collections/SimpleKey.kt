package po.misc.collections



class SimpleKey (
    val name: String,
    val type: Int
):Comparable<SimpleKey> {


    val key: String
        get() = "CompositeKey(${name}:${type})"

    override fun toString(): String = key

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleKey) return false
        return key == other.key &&
                type == other.type
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

