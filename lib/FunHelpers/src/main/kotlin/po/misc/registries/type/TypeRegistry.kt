package po.misc.registries.type

import po.misc.exceptions.ManagedException
import po.misc.interfaces.ValueBased
import po.misc.types.castOrThrow
import po.misc.types.getOrThrow
import po.misc.types.safeCast
import kotlin.reflect.typeOf

class TypeRegistry {

    @PublishedApi
    internal val registry = mutableMapOf<ValueBased, TypeRecord<*>>()

    inline fun <reified T: Any> addRecord(element: ValueBased):TypeRecord<T>{
        val record =  TypeRecord.createRecord<T>(element)
        registry[element] = record
        return record
    }


    fun getSimpleName(param : ValueBased): String{
       return registry[param]?.simpleName?:"Unavailable"
    }

    inline fun <reified T: Any,  reified E: ManagedException> getRecord(): TypeRecord<T>?{
        val key = T::class.qualifiedName
        val consRecord = registry.values.firstOrNull { it.qualifiedName  == key}
            .getOrThrow<TypeRecord<*>, E>("Record with key: $key does not exist in registry")

        val casted = consRecord.safeCast<TypeRecord<T>>()
        return casted
    }

    inline fun <reified T: Any, reified E: ManagedException> getRecordOrThrow(): TypeRecord<T>{
        val consRecord =  getRecord<T , E>()
        val casted =  consRecord.castOrThrow<TypeRecord<T>, E>("Record  with key: ${T::class.qualifiedName} can not be casted to type ${T::class.simpleName}")
        return casted
    }


    inline fun <T: Any, reified E: ManagedException> getRecord(element: ValueBased): TypeRecord<T>{
        val consRecord = registry.values.firstOrNull { it.element == element}
            .getOrThrow<TypeRecord<*>, E>("Record for value $element does not exist in registry")
        val casted = consRecord.castOrThrow<TypeRecord<T>, E>()
        return casted
    }

    @JvmName("getRecordNullableResult")
    fun <T: Any> getRecord(element: ValueBased): TypeRecord<T>?{
        val casted = registry.values.firstOrNull { it.element == element}?.safeCast<TypeRecord<T>>()
        return casted
    }

    inline fun <reified T : Any> contains(element: ValueBased): Boolean =
        T::class.qualifiedName?.let { registry.containsKey(element) } == true

}