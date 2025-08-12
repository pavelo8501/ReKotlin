package po.misc.data.json.models


import po.misc.data.helpers.withIndent
import po.misc.data.logging.LogEmitter
import po.misc.types.TypeData
import po.misc.types.castListOrManaged
import po.misc.types.castOrManaged
import po.misc.types.safeCast
import kotlin.reflect.KProperty1


sealed class JsonObjectBase<T: Any>(
    @PublishedApi internal val typeData: TypeData<T>,
    protected val key: String
): LogEmitter{
    abstract val formatter : (String)-> String
    val records: MutableList<JsonRecord<T, *>> = mutableListOf()
    @PublishedApi
    internal val jObjects : MutableList<JsonObject<*>> = mutableListOf()

    abstract fun toJson(receiver: T): String

    fun <T2: Any> getObjectsOfType(typeData: TypeData<T2>): List<JsonObjectBase<T2>>{
       val result = jObjects.filter { it.typeData ==  typeData}
       return result.castListOrManaged(typeData.kClass)
    }

}

class JsonObject<T: Any>(
    typeData: TypeData<T>
):JsonObjectBase<T>(typeData, ""){

    override val formatter : (String) -> String = {records->  records.withIndent(2, prefix = "{", postfix = "}") }
    val jsonLists: MutableList<JsonList<T, *>> =mutableListOf()


    fun <V: Any> createRecord(property: KProperty1<T, V>){
        records.add(JsonRecord(typeData, property))
    }

    inline fun <reified T2: Any, V: Any> createList(property: KProperty1<T, List<T2>>,  vararg listProperties: KProperty1<T2, V>){
        val list = JsonList<T, T2>(typeData, TypeData.create<T2>(), property)
        listProperties.forEach {
            list.createRecord(it)
        }
        jsonLists.add(list)
    }

    private fun processLists(receiver: T): String{
        var result: String = ""
        jsonLists.forEach {list->
            result += list.receiverToList(receiver)
        }
        return result
    }

    override fun toJson(receiver: T): String{
        val records = records.joinToString(separator = ",") { it.toJson(receiver) }
        var formattedString = formatter(records)
        formattedString +=  processLists(receiver)
        return formattedString
    }
}

/**
 * Must produce following format
 * ``"meta_tags": [
 *         { JsonRecord },
 *         ...
 *         ...
 *   ]``
 */
class JsonList<T: Any, LT: Any>(
    val parentTypeData : TypeData<T>,
     nestedTypeData: TypeData<LT>,
    val  listProperty: KProperty1<T, List<LT>>,
):JsonObjectBase<LT>(nestedTypeData, listProperty.name){

    override val formatter: (String) -> String = {records->  "[$records]"  }

    init {
        val nested =  createObject()
        jObjects.add(nested)
    }

    private fun createObject():JsonObject<LT>{
        return JsonObject(typeData)
    }

    fun <V: Any> createRecord(property: KProperty1<LT, V>){
        val found = jObjects.firstOrNull()?.safeCast<JsonObject<LT>>()
        found?.createRecord(property)
    }

    fun receiverToList(receiver: Any): String{
        val casted = receiver.castOrManaged(parentTypeData.kClass, this)
        val dataList = listProperty.get(casted)
        val lists =  dataList.joinToString(separator = ",") { toJson(it) }
        val formattedRecords = formatter(lists)
        return "\"$key\": $formattedRecords"
    }

    override fun toJson(receiver: LT): String{
        val objectList = getObjectsOfType(typeData)
        return objectList.joinToString(separator = ",") { it.toJson(receiver) }
    }
}






