package po.misc.data.json.models

import po.misc.data.json.formatJsonSafe
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1

class JsonRecord<T: Any, V: Any>(
    val typeData: TypeToken<T>,
    val property: KProperty1<T, V>
){
    val key: String = property.name

    private fun getValueJsonSafe(value:V): String{
        return formatJsonSafe(value)
    }

    fun recordToJson(receiver: T): String{
        val formattedValue = getValueJsonSafe(property.get(receiver))
        return "\"${key}\":${formattedValue}"
    }

    companion object{

        inline fun <reified T: Any, V: Any> create(
            property: KProperty1<T, V>
        ):JsonRecord<T, V>{
            return JsonRecord(TypeToken.create<T>(), property)
        }
    }
}