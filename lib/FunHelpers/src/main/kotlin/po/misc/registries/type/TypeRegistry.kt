package po.misc.registries.type

import po.misc.interfaces.ValueBased
import po.misc.types.TypeData
import po.misc.types.TypeRecord
import po.misc.types.castOrManaged
import po.misc.types.getOrManaged
import po.misc.types.getOrThrow
import po.misc.types.safeCast
import kotlin.reflect.KClass


@Deprecated("InEfficient", level = DeprecationLevel.WARNING)
class TypeRegistry {

    @PublishedApi
    internal val registry = mutableMapOf<ValueBased, TypeRecord<*>>()

    inline fun <reified T : Any> addRecord(key: ValueBased): TypeRecord<T> {
        val record = TypeRecord.createRecord<T>(key)
        registry[key] = record
        return record
    }

    fun <T : Any> addRecord(key: ValueBased, record:TypeRecord<T>): TypeRecord<T> {
        registry[key] = record
        return record
    }

    inline fun <T : Any> getRecord  (KClass: KClass<T>, exceptionProvider:(String)-> Throwable): TypeData<T> {

        TODO("Refactro or depreciate")
    }

    @JvmName("getRecordByValueNullableResult")
    fun <T : Any> getRecord(key: ValueBased): TypeRecord<T>? {
        val casted = registry[key]?.safeCast<TypeRecord<T>>()
        return casted
    }

    inline fun <reified T : Any> contains(key: ValueBased): Boolean =
        T::class.qualifiedName?.let { registry.containsKey(key) } == true
}