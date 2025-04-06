package po.managedtask.classes.notification


interface JasonStringSerializable {

    fun JasonStringSerializable.toJsonLike(): String {
        val raw = this.toString()
        val prefixRemoved = raw.substringAfter("(").substringBeforeLast(")")
        val entries = prefixRemoved.split(", ")
        val json = entries.joinToString(", ") {
            val (k, v) = it.split("=")
            "\"$k\": ${formatJsonValue(v)}"
        }
        return "{$json}"
    }

    private fun formatJsonValue(v: String): String {
        return if (v.matches(Regex("-?\\d+(\\.\\d+)?")) || v == "true" || v == "false") {
            v
        } else {
            "\"$v\""
        }
    }
}

