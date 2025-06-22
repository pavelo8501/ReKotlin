package po.misc.reflection.objects.components


import po.misc.collections.StaticTypeKey
import po.misc.data.styles.SpecialChars
import po.misc.reflection.objects.Composed
import po.misc.reflection.objects.ObjectManager
import po.misc.reflection.properties.PropertyIOBase

class ObjectsMap<E: Enum<E>,>(
    val thisKey: E,
    val hostingObject:ObjectManager<E, *>,
  // val initialList: List<PropertyIOBase<T, Any>> = emptyList()
):AbstractMutableMap<StaticTypeKey<*>, ObjectManager<*, Composed>>() {

    val className: String = hostingObject.sourceClass.className
    val backingMap: MutableMap<StaticTypeKey<*>, ObjectManager<*, Composed>> = mutableMapOf()
    override val entries: MutableSet<MutableMap.MutableEntry<StaticTypeKey<*>,  ObjectManager<*, Composed>>>
        get() {
            return backingMap.entries
        }



    override fun put(key: StaticTypeKey<*>, value: ObjectManager<*, Composed>): ObjectManager<*, Composed>? {
        return backingMap.put(key, value)
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
