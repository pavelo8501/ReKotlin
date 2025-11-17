package po.misc.properties


class LayeredConfigValue<T>(
    private var localValue: T? = null,
    val parent: LayeredConfigValue<T>? = null
) {
    private val children = mutableListOf<LayeredConfigValue<T>>()

    init { parent?.children?.add(this) }

    val value: T
        get() = localValue ?: parent?.value ?: error("No value provided")

    fun set(value: T) {
        localValue = value
        children.forEach { it.onParentUpdated(value) }
    }

    private fun onParentUpdated(newValue: T) {
        if (localValue == null) {
            children.forEach { it.onParentUpdated(newValue) }
        }
    }
}