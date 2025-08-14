package po.misc.data.json.extensions



import po.misc.data.json.models.JsonObject
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

inline fun <T: Any, reified ST: Any, V: Any> JsonObject<T, T>.buildSubArray(
    kClass: KClass<ST>,
    property: KProperty1<T, List<ST>>,
    vararg listProperties: KProperty1<ST, V>,
    builder:JsonObject<ST, T>.()-> Unit
): JsonObject<T, ST>{

    createObject(property, *listProperties)


    TODO("In refactor")

}