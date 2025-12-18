package po.misc.collections

import po.misc.context.CTX
import po.misc.data.NameValue

class CompositeKey (
    val component: CTX,
    val type: NameValue
):Comparable<CompositeKey> {


    val key: String
        get() = "CompositeKey(${component.completeName}:${type})"

    override fun toString(): String = key


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CompositeKey) return false
        return component.completeName == other.component.completeName &&
                component.completeName == other.component.completeName &&
                type == other.type
    }

    override fun hashCode(): Int {
        var result = component.completeName.hashCode()
        result = 31 * result + component.completeName.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun compareTo(other: CompositeKey): Int {
        val componentComparison = component.completeName.compareTo(other.component.completeName)
        return if (componentComparison != 0) componentComparison
        else type.value.compareTo(other.type.value)
    }
}

