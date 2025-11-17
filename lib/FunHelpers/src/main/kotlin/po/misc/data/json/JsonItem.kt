package po.misc.data.json

import po.misc.data.styles.SpecialChars

/**
 * Base interface for all structured JSON items.
 * Represents either a JSON record, object, or array.
 */
interface JsonItem{
    val idRecord: JsonIDRecord
}

/**
 * Enum representing the type of JSON entity.
 */
enum class JsonID {
    /** A single key-value pair like "key": "value" */
    Record,
    /** A JSON object like { "a": 1, "b": 2 } */
    Object,
    /** A JSON array like [1, 2, 3] */
    Array
}

/**
 * Metadata record for identifying JSON item context.
 *
 * @property jsonId The type of the JSON item (Record, Object, Array)
 * @property originalObjectName Optional source class or origin tag
 */
data class JsonIDRecord(
    val jsonId:JsonID,
    var originalObjectName: String = "",
)

/**
 * Represents a single JSON key-value pair.
 *
 * @param key The name of the field
 * @param value The associated value (any type)
 */
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
 * Represents a JSON object containing key-value records and/or nested objects.
 *
 * @param key The key name for this object when embedded in a parent
 */
class JObject(
    val key: String,
): JsonItem{

   override val idRecord: JsonIDRecord = JsonIDRecord(JsonID.Object)
   private val records : MutableList<JsonItem> = mutableListOf()

    /**
     * Adds a single key-value record to the object.
     * @return this instance for chaining
     */
    fun addRecord(record: JRecord):JObject{
        records.add(record)
        return this
    }

    /**
     * Adds a nested JSON object as a child.
     * @return this instance for chaining
     */
    fun addObject(jObject: JObject):JObject{
        records.add(jObject)
        return this
    }

    /**
     * Returns the full JSON string of this object with all records serialized.
     */
    override fun toString(): String {
      val formattedRecords = records.joinToString(prefix = "{", separator = ", ", postfix = "}")
      val selfName = "\"${key}\""
      return  "${selfName}:${formattedRecords}"
    }
}

/**
 * Represents a JSON array holding other JSON items (objects or records).
 *
 * @param holder Optional parent holder to which this array is linked.
 */
class JArray(
    val holder: JsonHolder?
):JsonItem {

    override val idRecord: JsonIDRecord = JsonIDRecord(JsonID.Array)
    private var hostingHolder: JsonHolder? = holder
    private val items: MutableList<JsonItem> = mutableListOf()

    /**
     * Sets the reference back to the hosting holder.
     */
    internal fun setHostingHolder(holder: JsonHolder) {
        hostingHolder = holder
    }

    /**
     * Adds a JSON object to the array.
     */
    fun addObject(jObject: JObject) {
        items.add(jObject)
    }

    /**
     * Adds a key-value record to the array.
     */
    fun addRecord(jRecord: JRecord) {
        items.add(jRecord)
    }

    /**
     * Adds a batch of items (records or objects) to the array.
     */
    fun addItems(jItems: List<JsonItem>) {
        items.addAll(jItems)
    }

    /**
     * Returns the JSON array as a string: [item1, item2, ...]
     */
    override fun toString(): String {
        return items.joinToString(prefix = "[", postfix = "]")
    }
}

/**
 * Holds and manages multiple JSON arrays.
 * Acts as a session/context container during JSON construction.
 *
 * @property activeArray The current array being built into
 */
class JsonHolder(var activeArray: JArray = JArray(null)) {

    private val jsonArrays: MutableList<JArray> = mutableListOf()

    /**
     * Finalizes the current array and starts a new one.
     * @return the newly created active array
     */
    fun startNewArray(): JArray {
        activeArray.setHostingHolder(this)
        jsonArrays.add(activeArray)
        activeArray = JArray(this)
        return activeArray
    }

    /**
     * Adds a new record to the active array.
     */
    fun addRecord(key: String, value: Any): JsonHolder {
        activeArray.addRecord(JRecord(key, value))
        return this
    }

    /**
     * Adds an already constructed record to the active array.
     */
    fun addRecord(item: JRecord): JsonHolder {
        activeArray.addRecord(item)
        return this
    }

    /**
     * Adds a full JSON object to the active array.
     */
    fun addJsonObject(jObject: JObject): JsonHolder {
        activeArray.addObject(jObject)
        return this
    }

    /**
     * Serializes all accumulated arrays into a complete JSON string.
     * If only one array is present, it is returned directly.
     * If multiple arrays exist, they are joined by commas.
     */
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
            printStrings.joinToString(separator = ",", postfix = SpecialChars.NEW_LINE)
        }
    }
}

