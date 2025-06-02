package po.misc.interfaces

import po.misc.collections.CompositeKey


interface ValueBased{
    val value: Int
}

abstract class ValueBasedClass(val value: Int){


//
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValueBased) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        return 31 * value.hashCode()
    }
//
//    override fun compareTo(other: CompositeKey): Int {
//        val componentComparison = component.completeName.compareTo(other.component.completeName)
//        return if (componentComparison != 0) componentComparison
//        else type.value.compareTo(other.type.value)
//    }

}