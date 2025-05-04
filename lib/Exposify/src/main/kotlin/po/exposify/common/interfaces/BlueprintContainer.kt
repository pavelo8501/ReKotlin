package po.exposify.common.interfaces

import po.exposify.common.classes.ClassData
import po.exposify.common.classes.ConstructorBuilder
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1

interface BlueprintContainer<T: Any> {

    fun getConstructor(): KFunction<T>
    fun initialize()
    fun getClass(): KClass<T>
    fun setConstructor(constructor : KFunction<T>)
    fun addParameter(param : KParameter)
    fun <N: Any> setNestedMap(map : Map<String, Map<String, ClassData<N>>>)

    fun getConstructorArgs(): Map<KParameter, Any?>
}
