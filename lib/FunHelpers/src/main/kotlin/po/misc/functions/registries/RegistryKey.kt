package po.misc.functions.registries

import po.misc.functions.registries.models.TaggedSubscriber


interface RegistryKey{
    val requireOnce: Boolean
    fun matchesWildcard(other: TaggedSubscriber<*>): Boolean
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}