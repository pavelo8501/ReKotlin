package po.misc.reflection.objects.components


import po.misc.collections.StaticTypeKey
import po.misc.data.styles.SpecialChars
import po.misc.reflection.objects.Composed
import po.misc.reflection.properties.PropertyIOBase
import kotlin.reflect.KClass

class KSurrogate<E: Enum<E>, T: Composed>(
    val thisKey: E,
    internal val source:T,
    internal val hooks:ManagerHooks,
    val initialList: List<PropertyIOBase<T, Any>> = emptyList()
):AbstractMutableMap<String, PropertyIOBase<T, Any>>() {

    val clazz: KClass<T> by lazy { source::class as KClass<T> }
    val typeKey : StaticTypeKey<T> = StaticTypeKey.createTypeKey(clazz)
    val backingMap: MutableMap<String, PropertyIOBase<T, Any>> = mutableMapOf()
    override val entries: MutableSet<MutableMap.MutableEntry<String, PropertyIOBase<T, Any>>>
        get() {
            return backingMap.entries
        }
    val className: String = clazz.simpleName.toString()

    init {
        initialList.forEach {
            backingMap.put(it.propertyName, it)
        }
    }

    override fun put(key: String, value: PropertyIOBase<T, Any>): PropertyIOBase<T, Any>? {
        return backingMap.put(key, value)
    }

    fun <V : Any> addProperty(property: PropertyIOBase<T, Any>) {
        backingMap[property.propertyName] = property
    }

    fun updateData(dataObject: T) {
        backingMap.values.forEach { ioProperty ->
            ioProperty.initialize(dataObject)
            ioProperty.flushBuffer()
            hooks.onPropertyUpdated?.invoke(ioProperty, ioProperty.currentValue.toString())
        }
    }

    override fun toString(): String {
        val personalStr = "KSurrogate<${thisKey.name}, ${className}>${SpecialChars.NewLine.char}"
        val propertyData = backingMap.values.map { it.toString() }
        return propertyData.joinToString(
            prefix = personalStr,
            separator = SpecialChars.NewLine.char
        )
    }
}
