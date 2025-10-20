package po.misc.data.json.models


import po.misc.data.helpers.withIndent
import po.misc.functions.dsl.DSLBlockMarker
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1

internal inline fun <T: Any>  JsonObject<*, T>.doWithNested(
    block: JsonObject<*, T>.()-> String
):String{
   return block.invoke(this)
}

@DSLBlockMarker
class JsonObject<T: Any, TP: Any>(
   val typeData: TypeToken<T>,
   val listProperty: KProperty1<TP, List<T>>? = null
){

    val records: MutableList<JsonRecord<T, *>> = mutableListOf()
    val nestedObjects: MutableList<JsonObject<*, T>> =mutableListOf()
    val hasNested: Boolean get() =  nestedObjects.isNotEmpty()

    fun <V: Any> createRecord(property: KProperty1<T, V>){
        records.add(JsonRecord(typeData, property))
    }

    inline fun <reified T2: Any, V: Any> createObject(property: KProperty1<T, List<T2>>,  vararg listProperties: KProperty1<T2, V>){
        val list = JsonObject<T2, T>(TypeToken.create<T2>(), property)

        listProperties.forEach {
            list.createRecord(it)
        }
        nestedObjects.add(list)
    }

    private fun processNested(receiver: T): String{
        val result = nestedObjects.joinToString {nested->
            nested.doWithNested {
                toJsonAsNested(receiver)
            }
        }
        return result
    }

    fun toJsonAsNested(receiver: TP): String {
        val property = listProperty
        if (property != null) {
            val records = listProperty.get(receiver).joinToString(separator = ",") { data ->
                toJson(data)
            }
            val formattedRecords = "\"${property.name}\": [$records]"
            return formattedRecords
        } else {
            return "[]"
        }
    }

    fun toJson(receiver: T): String{
       var upperResult = ""

       val result = if(!hasNested){
            val records = records.joinToString(prefix = "{", postfix = "}",  separator = ",") { it.recordToJson(receiver) }
            records
        }else{
           val records = records.joinToString(prefix = "{",   separator = ",") { it.recordToJson(receiver) }
            val nestedFormatted = processNested(receiver).withIndent(2, prefix =",",  postfix = "}")
           records + nestedFormatted
        }
        upperResult += result

        return upperResult
    }
}


