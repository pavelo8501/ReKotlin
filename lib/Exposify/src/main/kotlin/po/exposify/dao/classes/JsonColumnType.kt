package po.exposify.dao.classes

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.ColumnType
import org.postgresql.util.PGobject
import po.exposify.DatabaseManager
import po.misc.serialization.SerializerInfo
import po.misc.serialization.toSerializerInfo
import po.misc.types.TypeData
import kotlin.reflect.KType
import kotlin.reflect.typeOf


inline fun <reified T: Any> jsonColumnList(serializer: KSerializer<T>): JsonColumnList<T>{
  return  JsonColumnList<T>(typeOf<List<T>>() ,serializer)
}

inline fun <reified T: Any> jsonColumn(serializer: KSerializer<T>): JsonColumn<T>{
    return  JsonColumn(typeOf<T>() ,serializer)
}

sealed class JsonColumnBase<T: Any>(private val serializer: KSerializer<T>): ColumnType<T>() {

    abstract val serializerInfo: SerializerInfo<T>

    override fun sqlType(): String = "JSONB"

    override fun valueFromDB(value: Any): T = when (value) {
        // Extract JSON from PGobject
        is PGobject -> {
            val raw = value.value ?: "[]"
            Json.Default.decodeFromString(serializer, raw)
        }
        is List<*> -> {
            @Suppress("UNCHECKED_CAST")
            value as T
        }
        // Handle case where Exposed gives a String
        is String -> {
            Json.Default.decodeFromString(serializer, value)
        }
        else -> error("Unexpected JSON type: ${value::class}")
    }

    override fun notNullValueToDB(value: T): Any {
        val jsonString = Json.Default.encodeToString(serializer, value)
        return PGobject().apply {
            type = "jsonb"
            this.value = jsonString
        }
    }

    override fun nonNullValueToString(value: T): String {
        return "'${Json.Default.encodeToString(serializer, value)}'"
    }

    override fun valueToDB(value: T?): Any? {
        return if (value != null) {
            notNullValueToDB(value)
        } else {
            null
        }
    }
}

class JsonColumnList<T: Any> @PublishedApi internal constructor(
    val sourceType: KType,
    private val serializer: KSerializer<T>,
): JsonColumnBase<List<T>>(ListSerializer(serializer)){

    override val serializerInfo: SerializerInfo<List<T>>

        get() = toSerializerInfo(sourceType , ListSerializer(serializer), true)
    init {
        DatabaseManager.provideSerializer(serializerInfo)
    }
}

class JsonColumn<T: Any> @PublishedApi internal constructor(
    val sourceType: KType,
    private val serializer: KSerializer<T>,
) : JsonColumnBase<T>(serializer){

    override val serializerInfo: SerializerInfo<T>
        get() = toSerializerInfo(sourceType, serializer, false)

    init {
        DatabaseManager.provideSerializer(serializerInfo)
    }
}
