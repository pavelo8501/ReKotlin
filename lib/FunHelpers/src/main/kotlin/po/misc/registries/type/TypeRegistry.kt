package po.misc.registries.type

import po.misc.exceptions.ManagedException
import po.misc.types.castOrThrow
import po.misc.types.getOrThrow
import po.misc.types.safeCast
import kotlin.reflect.typeOf

class TypeRegistry {

    @PublishedApi
    internal val registry = mutableMapOf<String, TypeRecord<*>>()

    inline fun <reified T: Any> addRecord(name: String){
        registry[T::class.qualifiedName.toString()] =  TypeRecord(name, T::class, typeOf<T>())
    }

    inline fun <reified T: Any,  reified E: ManagedException> getRecord(): TypeRecord<T>?{
        val key = T::class.qualifiedName
        val consRecord = registry[key].getOrThrow<TypeRecord<*>, E>("Record with key: $key does not exist in registry")
        val casted = consRecord.safeCast<TypeRecord<T>>()
        return casted
    }

    inline fun <reified T: Any, reified E: ManagedException> getRecordOrThrow(): TypeRecord<T>{
        val consRecord =  getRecord<T,E>()
        val casted =  consRecord.castOrThrow<TypeRecord<T>, E>("Record  with key: ${T::class.qualifiedName} can not be casted to type ${T::class.simpleName}")
        return casted
    }


    inline fun <reified T: Any, reified E: ManagedException> getRecord(name: String): TypeRecord<T>?{
        val consRecord = registry.values.firstOrNull { it.name == name}
            .getOrThrow<TypeRecord<*>, E>("Record with name $name does not exist in registry")
        val casted = consRecord.safeCast<TypeRecord<T>>()
        return casted
    }

    inline fun <reified T: Any, reified E: ManagedException> getRecordOrThrow(name: String): TypeRecord<T>{
        val consRecord =  getRecord<T, E>(name)
        val casted =  consRecord.castOrThrow<TypeRecord<T>, E>("Record name: $name can not be casted to type ${T::class.simpleName}")
        return casted
    }


    inline fun <reified T : Any> contains(): Boolean =
        T::class.qualifiedName?.let { registry.containsKey(it) } == true

}