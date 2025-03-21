package po.exposify.extensions

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.ColumnType
import org.postgresql.util.PGobject

class JsonColumnType<T>(private val serializer: KSerializer<T>) : ColumnType<T>() {
    override fun sqlType(): String = "JSONB"

    override fun valueFromDB(value: Any): T = when (value) {
        is PGobject -> Json.decodeFromString(serializer, value.value!!) // Extract JSON from PGobject
        is String -> Json.decodeFromString(serializer, value) // Handle case where Exposed gives a String
        else -> error("Unexpected JSON type: ${value::class}")
    }

    override fun notNullValueToDB(value: T & Any): Any {
        val jsonString = Json.encodeToString(serializer, value)
        return PGobject().apply {
            type = "jsonb"
            this.value = jsonString
        }
    }

    override fun nonNullValueToString(value: T & Any): String {
        return "'${Json.encodeToString(serializer, value)}'"
    }

    override fun valueToDB(value: T?): Any? {
        return if (value != null) {
            notNullValueToDB(value)
        } else {
            null
        }
    }
}