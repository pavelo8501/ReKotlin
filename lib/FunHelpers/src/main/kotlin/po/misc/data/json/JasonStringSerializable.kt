package po.misc.data.json

interface JasonStringSerializable {
    fun toJson(): String
}

private fun formatJsonValue(value: String): String {
    return if (value.matches(Regex("-?\\d+(\\.\\d+)?")) || value == "true" || value == "false") {
        value
    } else {
        "\"$value\""
    }
}

fun toJsonLike(value: Any): String {
    val prefixRemoved = value.toString().substringAfter("(").substringBeforeLast(")")
    val entries = prefixRemoved.split(", ")
    val json = entries.joinToString(", ") {
        val parts = it.split("=", limit = 2)
        val key = parts.getOrNull(0)?.trim() ?: "unknown"
        val rawValue = parts.getOrNull(1)?.trim() ?: ""
        "\"$key\": ${formatJsonValue(rawValue)}"
    }
    return "{$json}"
}

fun formatJsonSafe(value: Any?): String {
    return when (value) {
        null -> "null"
        is Number, is Boolean -> value.toString()
        is String -> "\"${value.replace("\"", "\\\"")}\""
        is JasonStringSerializable -> toJsonLike(value)
        else -> "\"${value.toString().replace("\"", "\\\"")}\""
    }
}
