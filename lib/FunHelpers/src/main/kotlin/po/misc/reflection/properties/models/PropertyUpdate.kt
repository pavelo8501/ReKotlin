package po.misc.reflection.properties.models

import po.misc.collections.BufferItem


data class PropertyUpdate<T: Any>(
    val propertyName: String,
    override val value: T
): BufferItem<T>()


