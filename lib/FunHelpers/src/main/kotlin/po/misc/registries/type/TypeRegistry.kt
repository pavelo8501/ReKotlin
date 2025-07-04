package po.misc.registries.type

import po.misc.exceptions.ManagedException
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.interfaces.ValueBasedClass
import po.misc.types.TypeRecord
import po.misc.types.castOrThrow
import po.misc.types.getOrThrow
import po.misc.types.safeCast
import kotlin.reflect.typeOf

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

    inline fun <T : Any, reified E : ManagedException> getRecord(key: ValueBased, exceptionProvider:(String)->E): TypeRecord<T> {
        val consRecord = registry[key]
            .getOrThrow<TypeRecord<*>, E>(null, exceptionProvider)
        val casted = consRecord.castOrThrow<TypeRecord<T>, E>(null, exceptionProvider)
        return casted
    }

    @JvmName("getRecordByValueNullableResult")
    fun <T : Any> getRecord(key: ValueBased): TypeRecord<T>? {
        val casted = registry[key]?.safeCast<TypeRecord<T>>()
        return casted
    }

    inline fun <reified T : Any> contains(key: ValueBased): Boolean =
        T::class.qualifiedName?.let { registry.containsKey(key) } == true
}