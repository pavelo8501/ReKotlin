package po.misc.registries.basic

import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwManageable
import po.misc.exceptions.throwManaged
import po.misc.interfaces.IdentifiableContext
import po.misc.interfaces.ValueBased
import po.misc.types.getOrThrow


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
   inline fun <reified EX: ManagedException, S: Enum<S>> getRecord(key: ValueBased, ctx: IdentifiableContext, source:S? = null):T {
       val item = registry[key]
       return item?: throwManageable<EX, S>("$key is not in the registry", source, ctx)
    }

    inline fun <reified T : Any> contains(key: ValueBased): Boolean =
        T::class.qualifiedName?.let { registry.containsKey(key) } == true
}