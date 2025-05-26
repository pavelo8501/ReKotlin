package po.misc.collections

import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased

class CompositeKey (
    val component: Identifiable,
    val type: ValueBased
):Comparable<CompositeKey> {


    val key: String
        get() = "CompositeKey(${component.qualifiedName}:${type.value})"

    override fun toString(): String = key

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CompositeKey) return false
        return component.qualifiedName == other.component.qualifiedName &&
                type.value == other.type.value
    }

    override fun hashCode(): Int {
        return 31 * component.qualifiedName.hashCode() + type.value.hashCode()
    }

    override fun compareTo(other: CompositeKey): Int {
        val componentComparison = component.qualifiedName.compareTo(other.component.qualifiedName)
        return if (componentComparison != 0) componentComparison
        else type.value.compareTo(other.type.value)
    }
}

