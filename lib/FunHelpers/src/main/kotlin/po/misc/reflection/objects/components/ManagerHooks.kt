package po.misc.reflection.objects.components

import po.misc.reflection.properties.PropertyIOBase

class ManagerHooks {

    internal var onNewMap: ((KSurrogate<*, *>) -> Unit)? = null
    fun newMap(hook: (KSurrogate<*, *>) -> Unit) {
        onNewMap = hook
    }

    internal var onPropertyUpdated: (PropertyIOBase<*,*>.(String) -> Unit)? = null
    fun propertyUpdated(hook: PropertyIOBase<*,*>.(String) -> Unit) {
        onPropertyUpdated = hook
    }
}