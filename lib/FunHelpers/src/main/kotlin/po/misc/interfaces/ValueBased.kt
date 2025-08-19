package po.misc.interfaces

import po.misc.collections.CompositeKey


interface ValueBased{
    val value: Int
}

abstract class ValueBase(override val value: Int): ValueBased{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValueBased) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        return 31 * value.hashCode()
    }
}

class ValueBasedClass(value: Int): ValueBase(value)

fun toValueBased(value: Int):ValueBasedClass{
   return ValueBasedClass(value)
}