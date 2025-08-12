package po.misc.data.json.extensions


import po.misc.data.json.models.JsonList
import po.misc.data.json.models.JsonObject
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

inline fun <T: Any, reified ST: Any, V: Any> JsonObject<T>.buildSubArray(
    kClass: KClass<ST>,
    property: KProperty1<T, List<ST>>,
    vararg listProperties: KProperty1<ST, V>,
    builder:JsonObject<ST>.()-> Unit
): JsonList<T, ST>{

    createList(property, *listProperties)

    TODO("In refactor")

}