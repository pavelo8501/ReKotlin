package po.misc.data.json

import po.misc.data.styles.SpecialChars


sealed interface JsonItem{
    val idRecord: JsonIDRecord
}

enum class JsonID {
    Record,
    Object,
    Array
}

data class JsonIDRecord(
    val jsonId:JsonID,
    var originalObjectName: String = "",
)

class JRecord(
    val key: String,
    val value: Any
): JsonItem{

    override val idRecord: JsonIDRecord = JsonIDRecord(JsonID.Record)

    val isNumeric: Boolean = value is Number
    val jsonString : String get() = "\"${key}\":${formatJsonSafe(value)}"

    override fun toString(): String {
        return jsonString
    }
}

/**
 * Represents Json Object (list of JRecord)
 */
class JObject(
    val key: String,
): JsonItem{

   override val idRecord: JsonIDRecord = JsonIDRecord(JsonID.Object)
   private val records : MutableList<JsonItem> = mutableListOf()

    fun addRecord(record: JRecord):JObject{
        records.add(record)
        return this
    }
    fun addObject(jObject: JObject):JObject{
        records.add(jObject)
        return this
    }

    override fun toString(): String {
      val formattedRecords = records.joinToString(prefix = "{", separator = ", ", postfix = "}")
      val selfName = "\"${key}\""
      return  "${selfName}:${formattedRecords}"
    }
}

/**
 * Represents array [JObject or JRecord]
 */
class JsonArray(
    val holder: JsonHolder?
):JsonItem {

    override val idRecord: JsonIDRecord = JsonIDRecord(JsonID.Array)
    private var hostingHolder: JsonHolder? = holder
    private val items: MutableList<JsonItem> = mutableListOf()

    internal fun setHostingHolder(holder: JsonHolder) {
        hostingHolder = holder
    }

    fun addObject(jObject: JObject) {
        items.add(jObject)
    }

    fun addRecord(jRecord: JRecord) {
        items.add(jRecord)
    }

    fun addItems(jItems: List<JsonItem>) {
        items.addAll(jItems)
    }

    override fun toString(): String {
        return items.joinToString(prefix = "[", postfix = "]")
    }
}

class JsonHolder(var activeArray: JsonArray = JsonArray(null)) {

    private val jsonArrays: MutableList<JsonArray> = mutableListOf()

    fun startNewArray(): JsonArray {
        activeArray.setHostingHolder(this)
        jsonArrays.add(activeArray)
        activeArray = JsonArray(this)
        return activeArray
    }

    fun addRecord(key: String, value: Any): JsonHolder {
        activeArray.addRecord(JRecord(key, value))
        return this
    }

    fun addRecord(item: JRecord): JsonHolder {
        activeArray.addRecord(item)
        return this
    }

    fun addJsonObject(jObject: JObject): JsonHolder {
        activeArray.addObject(jObject)
        return this
    }

    override fun toString(): String {
        startNewArray()
        val printStrings: MutableList<String> = mutableListOf()
        jsonArrays.forEach { array ->
            val itemStr = array.toString()
            printStrings.add(itemStr)
        }
        return if (printStrings.size == 1) {
            printStrings[0]
        } else {
            printStrings.joinToString(separator = ",", postfix = SpecialChars.NewLine.toString())
        }
    }
}

