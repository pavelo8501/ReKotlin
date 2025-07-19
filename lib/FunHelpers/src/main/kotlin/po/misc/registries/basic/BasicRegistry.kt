package po.misc.registries.basic

import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwManageable
import po.misc.context.CTX
import po.misc.interfaces.ValueBased


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


    inline fun <reified T : Any> contains(key: ValueBased): Boolean =
        T::class.qualifiedName?.let { registry.containsKey(key) } == true
}