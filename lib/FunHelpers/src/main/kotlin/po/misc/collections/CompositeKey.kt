package po.misc.collections

import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased

class CompositeKey (
    val component: Identifiable,
    val type: ValueBased
):Comparable<CompositeKey> {


    val key: String
        get() = "CompositeKey(${component.completeName}:${type.value})"

    override fun toString(): String = key


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CompositeKey) return false
        return component.sourceName == other.component.sourceName &&
                component.componentName == other.component.componentName &&
                type.value == other.type.value
    }

    override fun hashCode(): Int {
        var result = component.sourceName.hashCode()
        result = 31 * result + component.componentName.hashCode()
        result = 31 * result + type.value.hashCode()
        return result
    }

    override fun compareTo(other: CompositeKey): Int {
        val componentComparison = component.completeName.compareTo(other.component.completeName)
        return if (componentComparison != 0) componentComparison
        else type.value.compareTo(other.type.value)
    }
}

