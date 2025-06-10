package po.misc.registries.basic

import po.misc.exceptions.ManagedException
import po.misc.interfaces.ValueBased
import po.misc.types.getOrManaged


class BasicRegistry<T: Any> {

    @PublishedApi()
    internal val registry: MutableMap<ValueBased, T> = mutableMapOf()

    fun addRecord(key: ValueBased, value:T): BasicRegistry<T> {
        registry[key] = value
        return this
    }

    fun getRecord(key: ValueBased):T? {
        return registry[key]
    }

    @JvmName("getRecordReified")
   inline fun <reified E: ManagedException> getRecord(key: ValueBased):T {
      return registry[key].getOrManaged<T, E>("$key is not in the registry", null)
    }

    inline fun <reified T : Any> contains(key: ValueBased): Boolean =
        T::class.qualifiedName?.let { registry.containsKey(key) } == true
}