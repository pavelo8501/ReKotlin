package po.misc.data.json

object StringDefaultProvider : SerializationProvider<String> {
    override fun serialize(name: String, value: String): String =
        "\"$name\": ${formatJsonSafe(value)}"
}

object IntDefaultProvider : SerializationProvider<Int> {
    override fun serialize(name: String, value: Int): String =
        "\"$name\": $value"
}

object TrimmedQuotedStringProvider : SerializationProvider<String> {
    override fun serialize(name: String, value: String): String =
        "\"$name\": ${formatJsonSafe(value.trim())}"
}

object UppercaseProvider : SerializationProvider<String> {
    override fun serialize(name: String, value: String): String =
        "\"$name\": ${formatJsonSafe(value.uppercase())}"
}

object NanoTimeProvider : SerializationProvider<Long> {
    override fun serialize(name: String, value: Long): String =
        "\"$name\": ${formatJsonSafe("${value}ms")}"
}

object ElapsedTimeProvider : SerializationProvider<Float> {
    override fun serialize(name: String, value: Float): String =
        "\"$name\": ${formatJsonSafe("${value}ms")}"
}